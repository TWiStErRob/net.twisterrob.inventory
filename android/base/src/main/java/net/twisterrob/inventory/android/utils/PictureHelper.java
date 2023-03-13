package net.twisterrob.inventory.android.utils;

import java.io.File;
import java.util.regex.Pattern;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import static android.graphics.Color.*;

import androidx.annotation.*;

import net.twisterrob.android.content.ImageRequest;
import net.twisterrob.android.utils.tools.*;

public abstract class PictureHelper {
	private static final Logger LOG = LoggerFactory.getLogger(PictureHelper.class);

	private static final String CROPPED_SUFFIX = "_crop";
	private static final String EXTENSION_SEPARATOR = ".";
	private static final String EXTRACT_PARTS = "(.*)" + Pattern.quote(EXTENSION_SEPARATOR) + "(.*)";
	private static final float[] NEGATIVE = new float[] {
			-1, 0, 0, 0, 255,
			0, -1, 0, 0, 255,
			0, 0, -1, 0, 255,
			0, 0, 0, 1, 0
	};
	private Bitmap thumb;
	private Bitmap image;
	private File file;
	private File cropFile;
	private final Activity activity;

	public PictureHelper(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Given a transparent greyscale image this tints the blacks/greys with the given color, leaves white intact.
	 * <ul>
	 * <li>first negative hides the whites</li>
	 * <li>whites (originally black) are replaced to inverse of accent via a PorterDuff.MULTIPLY</li>
	 * <li>blacks (originally white) are not affected by multiply (<code>0 * c == c</code>)</li>
	 * <li>inverse of accent through another negative will become accent</li>
	 * </ul>
	 */
	public static @NonNull ColorMatrix tintMatrix(@ColorInt int color) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.postConcat(new ColorMatrix(NEGATIVE));
		matrix.postConcat(new ColorMatrix(new float[] {
				1 - red(color) / 255f, 0, 0, 0, 0,
				0, 1 - green(color) / 255f, 0, 0, 0,
				0, 0, 1 - blue(color) / 255f, 0, 0,
				0, 0, 0, alpha(color) / 255f, 0
		}));
		matrix.postConcat(new ColorMatrix(NEGATIVE));
		return matrix;
	}

	public static @NonNull ColorMatrix postAlpha(
			@FloatRange(from = 0, to = 1) float alpha, @NonNull ColorMatrix matrix) {
		matrix.postConcat(new ColorMatrix(new float[] {
				1, 0, 0, 0, 0,
				0, 1, 0, 0, 0,
				0, 0, 1, 0, 0,
				0, 0, 0, alpha, 0
		}));
		return matrix;
	}

	/**
	 * @see <a href="http://stackoverflow.com/a/31217267/253468">SO</a>
	 * @see <a href="https://gist.github.com/ro-sharp/49fd46a071a267d9e5dd">Gist</a>
	 */
	@SuppressWarnings("UnusedAssignment")
	public static int getColor(Object thing) {
		int seed = thing.hashCode();
		// Math.sin jumps big enough even when adding 1, because argument is radian and period is ~3
		int rand_r = (int)Math.abs(Math.sin(seed++) * 10000) & 0xFF;
		int rand_g = (int)Math.abs(Math.sin(seed++) * 10000) & 0xFF;
		int rand_b = (int)Math.abs(Math.sin(seed++) * 10000) & 0xFF;

		int r = Math.round((160 + rand_r) / 2);
		int g = Math.round((160 + rand_g) / 2);
		int b = Math.round((160 + rand_b) / 2);
		return argb(0xFF, r, g, b);
	}

	protected abstract File getTargetFile();

	protected File getTargetFileCropped() {
		File file = getTargetFile();
		String name = file.getName().replaceFirst(EXTRACT_PARTS, "$1");
		String ext = file.getName().replaceFirst(EXTRACT_PARTS, "$2");
		return new File(file.getParentFile(), name + CROPPED_SUFFIX + EXTENSION_SEPARATOR + ext);
	}

	public Intent startCapture() {
		file = getTargetFile();
		return new ImageRequest.Builder(activity).addCameraIntents(file).build().getIntent();
	}

	/**
	 * Back from chooser: code = 0, data = null
	 * Camera take picture (file, uri): code = -1, data = null, uri= file:///storage/emulated/0/Android/data/net.twisterrob.inventory/files/Pictures/Item_5_20140717_173719.jpg
	 * Gallery pick picture: code = -1, data = (action=null, data=content://media/external/images/media/1173, extras)
	 * mimeType=image/*
	 * selectedCount=1
	 * selectedItems=content://media/external/images/media/1173
	 * Total commander: data=content://com.ghisler.android.TotalCommander.files/storage/emulated/0/DCIM/Facebook/IMG_85501072011957.jpeg
	 * @return <code>true</code> if capture was successful
	 */
	public boolean endCapture(int resultCode, Intent intent) {
		LOG.trace("endCapture(resultCode={}, intent={}): {}\nextras={}", resultCode, intent, file,
				intent != null? StringerTools.toShortString(intent.getExtras()) : null);
		if (resultCode == Activity.RESULT_CANCELED) {
			file = null;
			return false;
		}
		file = processReceivedIntent(intent);
		return true;
	}

	public File getFile() {
		return cropFile != null? cropFile : file;
	}

	public Bitmap getBitmap() {
		return image;
	}

	public Bitmap getThumbnail() {
		return thumb;
	}

	public Intent startCrop() {
		cropFile = getTargetFileCropped();
		Intent intent = null;
		if (file != null && cropFile != null) {
			intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(Uri.fromFile(file), "image/*");
			intent.putExtra("crop", "true");
			intent.putExtra("scale", "true");
			intent.putExtra("scaleUpIfNeeded", true);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 256);
			intent.putExtra("outputY", 256);
			intent.putExtra("return-data", false);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, cropFile);
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			if (PackageManagerTools.resolveActivity(activity.getPackageManager(), intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
				//App.toastUser("Sorry, your device can't crop this image!");
				intent = null;
			}
		}
		return intent;
	}

	public boolean endCrop(int resultCode, Intent intent) {
		LOG.trace("endCrop(resultCode={}, intent={}): {}\nextras={}", resultCode, intent, cropFile,
				intent != null? StringerTools.toShortString(intent.getExtras()) : null);
		if (resultCode == Activity.RESULT_CANCELED) {
			cropFile = null;
			return false;
		}
		cropFile = processReceivedIntent(intent);
		return true;
	}

	private File processReceivedIntent(Intent intent) {
		File file = null;
		if (intent != null) {
			file = ImageTools.getFile(activity, intent.getData());
			thumb = IntentTools.getParcelableExtra(intent, "data", Bitmap.class);
		}
		return file;
	}
}
