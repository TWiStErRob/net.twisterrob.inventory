package net.twisterrob.inventory.android;

import java.io.File;
import java.util.*;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.AnyRes;
import android.util.TypedValue;

import static android.util.TypedValue.*;

import com.bumptech.glide.*;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.cache.*;
import com.bumptech.glide.load.model.ImageVideoWrapper;
import com.bumptech.glide.load.resource.bitmap.ImageVideoBitmapDecoder;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifResourceDecoder;
import com.bumptech.glide.load.resource.gifbitmap.*;
import com.bumptech.glide.module.GlideModule;

import net.twisterrob.android.content.glide.*;
import net.twisterrob.android.content.glide.LoggingListener.ResourceFormatter;

public interface Constants {
	@AnyRes int INVALID_RESOURCE_ID = 0;
	boolean DISABLE = Boolean.parseBoolean("false");
	String AUTHORITY_IMAGES = BuildConfig.APPLICATION_ID + ".images";

	class Paths {
		/** Warning: this is used inlined in paths_images.xml because path doesn't support string resources */
		private static final String INTERNAL_IMAGES_FOLDER = "images";
		public static final String DRIVE_HOME_FOLDER = "Magic Home Inventory";
		public static File getImageDirectory(Context context) {
			return new File(context.getFilesDir(), INTERNAL_IMAGES_FOLDER);
		}
		public static File getImageFile(Context context, String image) {
			if (image == null) {
				return null;
			}
			File imageFile = new File(image);
			return imageFile.isAbsolute()? imageFile : new File(Paths.getImageDirectory(context), image);
		}
		public static String getImagePath(Context context, String image) {
			File imageFile = getImageFile(context, image);
			return imageFile != null? imageFile.getAbsolutePath() : null;
		}
		public static String getExportFileName() {
			return String.format(Locale.ROOT, "MagicHomeInventory-%tF_%<tH-%<tM-%<tS.zip", Calendar.getInstance());
		}
		public static File getPhoneHome() {
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		}
	}

	interface Prefs {
		@Deprecated
		String DRIVE_FOLDER_ID = "driveRootFolder";
		String CURRENT_LANGUAGE = "currentLanguage";
		String LAST_EXPORT_DRIVE_ID = "lastExportDriveId";
	}

	class Dimensions {
		/** dp */
		private static final int ACTIONBAR_ICON_SIZE = 48;
		/** dp */
		private static final int ACTIONBAR_ICON_PADDING = 4;

		public static int getActionbarIconSize(Context context) {
			return (int)dpToPixels(context, ACTIONBAR_ICON_SIZE);
		}
		public static int getActionbarIconPadding(Context context) {
			return (int)dpToPixels(context, ACTIONBAR_ICON_PADDING);
		}
		private static float dpToPixels(Context context, int dp) {
			return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
		}
	}

	class Pic {
		public static final DrawableRequestBuilder<Integer> SVG_REQUEST = Glide
				.with(App.getAppContext())
				.fromResource()
				.listener(
						new LoggingListener<Integer, GlideDrawable>("SVG", new ResourceFormatter(App.getAppContext())))
				.decoder(getSvgDecoder())
				.crossFade()
				.error(R.drawable.category_unknown);

		public static final DrawableRequestBuilder<String> IMAGE_REQUEST = Glide
				.with(App.getAppContext())
				.fromString()
				.listener(new LoggingListener<String, GlideDrawable>("image"))
				.crossFade()
				.error(R.drawable.image_error);

		private static ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> getSvgDecoder() {
			Context context = App.getAppContext();
			BitmapPool pool = Glide.get(context).getBitmapPool();
			return new GifBitmapWrapperResourceDecoder(
					new ImageVideoBitmapDecoder(
							new SvgBitmapDecoder(pool),
							null /*fileDescriptorDecoder*/
					),
					new GifResourceDecoder(context, pool),
					pool
			);
		}

		public static class GlideSetup implements GlideModule {
			@Override public void applyOptions(final Context context, GlideBuilder builder) {
				if (BuildConfig.DEBUG) {
					builder.setDiskCache(new DiskCache.Factory() {
						@Override public DiskCache build() {
							final File cacheDir = new File(context.getExternalCacheDir(), "image_manager_disk_cache");
							return DiskLruCacheWrapper.get(cacheDir, 250 * 1024 * 1024);
						}
					});
				}
			}
			@Override public void registerComponents(Context context, Glide glide) {
			}
		}
	}
}
