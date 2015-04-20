package net.twisterrob.inventory.android;

import java.io.*;
import java.util.*;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.*;

import com.bumptech.glide.*;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.bumptech.glide.load.resource.bitmap.ImageVideoBitmapDecoder;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifResourceDecoder;
import com.bumptech.glide.load.resource.gifbitmap.GifBitmapWrapperResourceDecoder;
import com.bumptech.glide.module.GlideModule;

import net.twisterrob.android.content.glide.*;
import net.twisterrob.android.content.glide.LoggingListener.ResourceFormatter;
import net.twisterrob.android.utils.tools.IOTools;

public interface Constants {
	boolean DISABLE = Boolean.parseBoolean("false");

	class Paths {
		private static final String INTERNAL_IMAGES_FOLDER = "images";
		/** Warning: this is used inlined in paths_share.xml because path doesn't support string resources */
		private static final String PUBLIC_SHARE_FOLDER_NAME = "share";
		public static final String BACKUP_XML_FILENAME = "data.xml";
		public static final String BACKUP_CSV_FILENAME = "data.csv";
		public static @NonNull File getImageDirectory(Context context) {
			if (Constants.DISABLE && BuildConfig.DEBUG) {
				File dir = context.getExternalFilesDir(INTERNAL_IMAGES_FOLDER);
				return dir != null? dir : new File(context.getFilesDir(), INTERNAL_IMAGES_FOLDER);
			} else {
				return new File(context.getFilesDir(), INTERNAL_IMAGES_FOLDER);
			}
		}
		public static @NonNull File getImageFile(Context context, @NonNull String image) {
			File imageFile = new File(image);
			return imageFile.isAbsolute()? imageFile : new File(Paths.getImageDirectory(context), image);
		}
		public static @Nullable String getImagePath(Context context, @Nullable String image) {
			if (image == null) {
				return null;
			}
			return getImageFile(context, image).getAbsolutePath();
		}
		public static @NonNull File getExportFile() throws IOException {
			Calendar now = Calendar.getInstance();
			String fileName = String.format(Locale.ROOT, "Inventory_%tF_%<tH-%<tM-%<tS.zip", now);
			File exportFolder = getPhoneHome();
			IOTools.ensure(exportFolder);
			return new File(exportFolder, fileName);
		}
		public static @NonNull File getPhoneHome() {
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		}

		public static File getTempImage(Context context) throws IOException {
			File tempDir = new File(context.getCacheDir(), PUBLIC_SHARE_FOLDER_NAME);
			IOTools.ensure(tempDir);
			File tempImage = new File(tempDir, "temp.jpg");
			if (tempImage.exists()) {
				if (!tempImage.isFile()) {
					throw new IOException(tempImage + " is not a file");
				}
				if (!tempImage.delete()) {
					throw new IOException("Cannot clean up " + tempImage);
				}
			}
			tempImage.deleteOnExit();
			return tempImage;
		}
	}

	interface Prefs {
		@Deprecated
		String DRIVE_FOLDER_ID = "driveRootFolder";
		String CURRENT_LANGUAGE = "currentLanguage";
		String LAST_EXPORT_DRIVE_ID = "lastExportDriveId";
	}

	class Pic {
		private static final LoggingListener<Integer, GlideDrawable> SVG_LOGGING_LISTENER =
				new LoggingListener<>("SVG", new ResourceFormatter(App.getAppContext()));
		private static final LoggingListener<String, GlideDrawable> IMAGE_LOGGING_LISTENER =
				new LoggingListener<>("image");

		private static <T> DrawableRequestBuilder<T> baseRequest(Class<T> clazz) {
			ModelLoader<T, InputStream> loader = Glide.buildModelLoader(clazz, InputStream.class, App.getAppContext());
			DrawableRequestBuilder<T> builder = Glide
					.with(App.getAppContext())
					.using((StreamModelLoader<T>)loader)
					.from(clazz)
					.animate(android.R.anim.fade_in)
					.error(R.drawable.image_error);
			if (DISABLE && BuildConfig.DEBUG) {
				builder = builder
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.skipMemoryCache(true)
				;
			}
			return builder;
		}

		public static final DrawableRequestBuilder<Integer> SVG_REQUEST = baseRequest(Integer.class)
				.listener(SVG_LOGGING_LISTENER)
				.decoder(getSvgDecoder());

		public static final DrawableRequestBuilder<String> IMAGE_REQUEST = baseRequest(String.class)
				.listener(IMAGE_LOGGING_LISTENER);

		private static GifBitmapWrapperResourceDecoder getSvgDecoder() {
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
//				if (BuildConfig.DEBUG) {
//					builder.setDiskCache(new DiskCache.Factory() {
//						@Override public DiskCache build() {
//							final File cacheDir = new File(context.getExternalCacheDir(), "image_manager_disk_cache");
//							return DiskLruCacheWrapper.get(cacheDir, 250 * 1024 * 1024);
//						}
//					});
//				}
			}
			@Override public void registerComponents(Context context, Glide glide) {
			}
		}
	}
}
