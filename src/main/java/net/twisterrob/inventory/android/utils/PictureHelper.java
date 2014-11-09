package net.twisterrob.inventory.android.utils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.App;

public abstract class PictureHelper {
	private static final Logger LOG = LoggerFactory.getLogger(PictureHelper.class);

	private static final String CROPPED_SUFFIX = "_crop";
	private static final String EXTENSION_SEPARATOR = ".";
	private static final String EXTRACT_PARTS = "(.*)" + Pattern.quote(EXTENSION_SEPARATOR) + "(.*)";
	private Bitmap thumb;
	private Bitmap image;
	private File file;
	private File cropFile;
	private final Activity activity;

	public PictureHelper(Activity activity) {
		this.activity = activity;
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
		List<Intent> camIntents = createCameraIntents(file);
		Intent chooserIntent = Intent.createChooser(createGalleryIntent(), null);
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, camIntents.toArray(new Parcelable[camIntents.size()]));
		return chooserIntent;
	}

	private List<Intent> createCameraIntents(File file) {
		Uri uri = Uri.fromFile(file);
		List<Intent> cameraIntents = new ArrayList<Intent>();
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		PackageManager packageManager = activity.getPackageManager();
		List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
		for (ResolveInfo res : listCam) {
			Intent intent = new Intent(captureIntent);
			intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			intent.setPackage(res.activityInfo.packageName);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			cameraIntents.add(intent);
		}
		return cameraIntents;
	}

	public static Intent createGalleryIntent() {
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		return galleryIntent;
	}

	/**
	 * Back from chooser: code = 0, data = null
	 * Camera take picture (file, uri): code = -1, data = null, uri= file:///storage/emulated/0/Android/data/net.twisterrob.inventory/files/Pictures/Item_5_20140717_173719.jpg
	 * Gallery pick picture: code = -1, data = (action=null, data=content://media/external/images/media/1173, extras)
	 * mimeType=image/*
	 * selectedCount=1
	 * selectedItems=content://media/external/images/media/1173
	 * Total commander: data=content://com.ghisler.android.TotalCommander.files/storage/emulated/0/DCIM/Facebook/IMG_85501072011957.jpeg
	 * @return
	 */
	public boolean endCapture(int resultCode, Intent intent) {
		LOG.trace("endCapture(resultCode={}, intent={}): {}\nextras={}", resultCode, intent, file,
				intent != null? AndroidTools.toString(intent.getExtras()) : null);
		if (resultCode == Activity.RESULT_CANCELED) {
			file = null;
			return false;
		}
		if (intent != null) {
			file = ImageTools.getFile(activity, intent.getData());
			Bundle extras = intent.getExtras();
			if (extras != null) {
				Object data = extras.get("data");
				if (data instanceof Bitmap) {
					thumb = (Bitmap)data;
				}
			}
		}
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
			if (activity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
				App.toast("Sorry, your device can't crop this image!");
				intent = null;
			}
		}
		return intent;
	}

	public boolean endCrop(int resultCode, Intent intent) {
		LOG.trace("endCrop(resultCode={}, intent={}): {}\nextras={}", resultCode, intent, cropFile,
				intent != null? AndroidTools.toString(intent.getExtras()) : null);
		if (resultCode == Activity.RESULT_CANCELED) {
			cropFile = null;
			return false;
		}
		if (intent != null) {
			cropFile = ImageTools.getFile(activity, intent.getData());
			Bundle extras = intent.getExtras();
			if (extras != null) {
				Object data = extras.get("data");
				if (data instanceof Bitmap) {
					thumb = (Bitmap)data;
				}
			}
		}
		return true;
	}
}
