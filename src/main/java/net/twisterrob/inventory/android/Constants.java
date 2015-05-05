package net.twisterrob.inventory.android;

import java.io.*;
import java.util.*;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

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
import com.bumptech.glide.signature.StringSignature;

import net.twisterrob.android.content.glide.*;
import net.twisterrob.android.content.glide.LoggingListener.ResourceFormatter;
import net.twisterrob.android.utils.tools.IOTools;

public interface Constants {
	boolean DISABLE = Boolean.parseBoolean("false");

	class Paths {
		/** Warning: this is used inlined in paths_share.xml because path doesn't support string resources */
		private static final String PUBLIC_SHARE_FOLDER_NAME = "share";
		public static final String BACKUP_XML_FILENAME = "data.xml";
		public static final String BACKUP_CSV_FILENAME = "data.csv";

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

		public static File getShareImage(Context context) throws IOException {
			return getTemporaryCacheFile(context, PUBLIC_SHARE_FOLDER_NAME, "share_", ".jpg");
		}
		public static File getTempImage(Context context) throws IOException {
			return getTemporaryCacheFile(context, "temp", "temp_", ".jpg");
		}
		private static File getTemporaryCacheFile(Context context, String folderName, String prefix, String suffix)
				throws IOException {
			File folder = new File(context.getCacheDir(), folderName);
			IOTools.ensure(folder);
			File file = new File(folder, prefix + 0 + suffix);
			// TODO figure out an alternative to deleteOnExit, before that:
			//noinspection ResultOfMethodCallIgnored, use the same image file over and over again
			file.delete();
			//File file = File.createTempFile(prefix, suffix, folder);
			file.deleteOnExit();
			return file;
		}
	}

	interface Prefs {
		String CURRENT_LANGUAGE = "currentLanguage";
		String LAST_EXPORT_DRIVE_ID = "lastExportDriveId";
	}

	class Pic {
		private static final LoggingListener<Integer, GlideDrawable> SVG_LOGGING_LISTENER =
				new LoggingListener<>("SVG", new ResourceFormatter(App.getAppContext()));
		private static final LoggingListener<Uri, GlideDrawable> IMAGE_LOGGING_LISTENER =
				new LoggingListener<>("image");

		public static <T> DrawableRequestBuilder<T> baseRequest(Class<T> clazz) {
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

		private static final DrawableRequestBuilder<Integer> SVG_REQUEST = baseRequest(Integer.class)
				.signature(new StringSignature(BuildConfig.VERSION_NAME))
				.dontAnimate()
				.listener(SVG_LOGGING_LISTENER)
				.decoder(getSvgDecoder());

		private static final DrawableRequestBuilder<Uri> IMAGE_REQUEST = baseRequest(Uri.class)
				.animate(android.R.anim.fade_in)
				.listener(IMAGE_LOGGING_LISTENER);

		public static DrawableRequestBuilder<Integer> svg() {
			return SVG_REQUEST.clone();
		}

		// TODO should be loading from Uris because it's toStringed everywhere
		public static DrawableRequestBuilder<Uri> jpg() {
			return IMAGE_REQUEST.clone();
		}

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
