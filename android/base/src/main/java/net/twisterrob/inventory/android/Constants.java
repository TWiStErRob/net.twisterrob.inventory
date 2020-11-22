package net.twisterrob.inventory.android;

import java.io.*;
import java.util.*;

import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;

import com.bumptech.glide.*;
import com.bumptech.glide.load.*;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.cache.*;
import com.bumptech.glide.load.model.*;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.bumptech.glide.load.resource.bitmap.ImageVideoBitmapDecoder;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifResourceDecoder;
import com.bumptech.glide.load.resource.gifbitmap.*;
import com.bumptech.glide.load.resource.transcode.GifBitmapWrapperDrawableTranscoder;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.signature.StringSignature;

import androidx.annotation.NonNull;
import androidx.core.content.*;
import androidx.core.graphics.ColorUtils;

import net.twisterrob.android.content.glide.*;
import net.twisterrob.android.content.glide.LoggingListener.ResourceFormatter;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.base.*;
import net.twisterrob.inventory.android.utils.PictureHelper;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public interface Constants {
	/**
	 * Turn off parts of the app permanently during build time regardless of flavor.
	 * Never change it to <code>true</code> rather remove the usage from code to enable that code path.
	 */
	boolean DISABLE = Boolean.parseBoolean("false"); // TODEL IDEA-157715

	class Paths {
		/** Warning: this is used inlined in paths_share.xml because path doesn't support string resources */
		private static final String PUBLIC_SHARE_FOLDER_NAME = "share";
		/** Warning: this is used inlined in paths_share.xml because path doesn't support string resources */
		private static final String PUBLIC_TEMP_FOLDER_NAME = "temp";
		public static final String BACKUP_DATA_FILENAME = "data.xml";

		public static @NonNull File getExportFile(File exportFolder) throws IOException {
			IOTools.ensure(exportFolder);
			return new File(exportFolder, getExportFileName(Calendar.getInstance()));
		}
		public static @NonNull String getExportFileName(Calendar now) {
			return String.format(Locale.ROOT, "Inventory_%tF_%<tH-%<tM-%<tS.zip", now);
		}

		@SuppressWarnings("deprecation") // see requestLegacyExternalStorage
		public static @NonNull File getPhoneHome() {
			StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
			try {
				// D/StrictMode: StrictMode policy violation; ~duration=17 ms: android.os.strictmode.DiskReadViolation
				// at java.io.File.isDirectory(File.java:845)
				// at net.twisterrob.java.io.IOTools.isValidDir(IOTools.java:375)
				File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				if (!IOTools.isValidDir(dir)) { // fall back to /sdcard
					dir = Environment.getExternalStorageDirectory();
				}
				if (!IOTools.isValidDir(dir)) { // fall back to /
					dir = Environment.getRootDirectory();
				}
				return dir;
			} finally {
				StrictMode.setThreadPolicy(originalPolicy);
			}
		}

		/**
		 * Make sure to add <code>.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)</code> to the intent.
		 * @param file from {@link #getShareFile}
		 * @return {@link Uri} to be shared in {@link Intent#setData(Uri)}
		 */
		public static @NonNull Uri getShareUri(@NonNull Context context, @NonNull File file) {
			String authority = AndroidTools.findProviderAuthority(context, FileProvider.class).authority;
			return FileProvider.getUriForFile(context, authority, file);
		}
		public static File getShareFile(@NonNull Context context, @NonNull String ext) throws IOException {
			return getTemporaryCacheFile(context, PUBLIC_SHARE_FOLDER_NAME, "share_", "." + ext);
		}
		public static File getShareImage(@NonNull Context context) throws IOException {
			return getShareFile(context, "jpg");
		}
		public static File getTempImage(@NonNull Context context) throws IOException {
			return getTemporaryCacheFile(context, PUBLIC_TEMP_FOLDER_NAME, "temp_", ".jpg");
		}
		private static File getTemporaryCacheFile(
				@NonNull Context context,
				@NonNull String folderName,
				@NonNull String prefix,
				@NonNull String suffix
		) throws IOException {
			StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
			try {
				File folder = new File(context.getCacheDir(), folderName);
				IOTools.ensure(folder);
				File file = new File(folder, prefix + 0 + suffix);
				// TODO figure out an alternative to deleteOnExit, until then:
				//noinspection ResultOfMethodCallIgnored, use the same image file over and over again
				//file.delete(); // don't delete because it causes strange behavior (edit, take, crop, take, back, save -> ENOENT)
				//File file = File.createTempFile(prefix, suffix, folder);
				file.deleteOnExit();
				return file;
			} finally {
				StrictMode.setThreadPolicy(originalPolicy);
			}
		}
	}

	class Pic {
		private static final DecodeFormat PREFERRED_FORMAT = DecodeFormat.PREFER_ARGB_8888;
		private static Pic instance;

		/**
		 * Achieve a ghost-like image, this is to be used where the user can replace it with a real photo.
		 */
		private final ColorFilter ghostFilter;
		/**
		 * Color somewhere between accent and accentDark.
		 * This means that it'll be visible even if those are used as a background where this tint is applied.
		 * There's no point in changing accentDark to this mixture, because then selection highlight won't work nicely. 
		 */
		private final ColorFilter tintFilter;

		private final DrawableRequestBuilder<Uri> imageRequest;
		private final DrawableRequestBuilder<Integer> svgRequest;
		private final DrawableRequestBuilder<Integer> svgRequestTinted;

		private Pic(@NonNull Context context, @NonNull String versionName) {
			context = context.getApplicationContext();
			DrawableRequestBuilder<Integer> baseSvgRequest = baseRequest(Integer.class, context)
					.dontAnimate()
					.signature(new StringSignature(versionName))
					.priority(Priority.HIGH)
					.decoder(getSvgDecoder(context));
			if (DISABLE && BuildConfig.DEBUG) {
				LoggingListener.ResourceFormatter formatter = new ResourceFormatter(context);
				baseSvgRequest.listener(new LoggingListener<Integer, GlideDrawable>("SVG", formatter));
			}
			ghostFilter = createGhostFilter(context);
			tintFilter = createTintFilter(context);

			svgRequest = baseSvgRequest
					.clone()
					.transcoder(new GifBitmapWrapperDrawableTranscoder(
							new FilteredGlideBitmapDrawableTranscoder(context, "primary-ghost", ghostFilter)
					));
			svgRequestTinted = baseSvgRequest
					.clone()
					.transcoder(new GifBitmapWrapperDrawableTranscoder(
							new FilteredGlideBitmapDrawableTranscoder(context, "accent-tint", tintFilter)
					));

			imageRequest = baseRequest(Uri.class, context)
					.animate(android.R.anim.fade_in)
					.priority(Priority.NORMAL);
			if (DISABLE && BuildConfig.DEBUG) {
				imageRequest.listener(new LoggingListener<Uri, GlideDrawable>("image"));
			}
		}

		private ColorMatrixColorFilter createGhostFilter(Context context) {
			return new ColorMatrixColorFilter(PictureHelper.postAlpha(0.33f,
					PictureHelper.tintMatrix(ContextCompat.getColor(context, R.color.primaryDark))
			));
		}
		private ColorMatrixColorFilter createTintFilter(Context context) {
			return new ColorMatrixColorFilter(PictureHelper.tintMatrix(
					ColorUtils.blendARGB(
							ContextCompat.getColor(context, R.color.accent),
							ContextCompat.getColor(context, R.color.accentDark),
							0.75f
					)
			));
		}
		
		public static DrawableRequestBuilder<Integer> svg() {
			return instance.svgRequestTinted.clone();
		}
		public static DrawableRequestBuilder<Integer> svgNoTint() {
			return instance.svgRequest.clone();
		}
		public static DrawableRequestBuilder<Uri> jpg() {
			return instance.imageRequest.clone();
		}
		public static ColorFilter tint() {
			return instance.tintFilter;
		}
		public static void init(@NonNull Context context, @NonNull String versionName) {
			instance = new Pic(context, versionName);
		}

		private <T> DrawableRequestBuilder<T> baseRequest(Class<T> clazz, Context context) {
			ModelLoader<T, InputStream> loader = Glide.buildModelLoader(clazz, InputStream.class, context);
			// FIXME replace this with proper Glide.with calls, don't use App Context
			DrawableRequestBuilder<T> builder = Glide
					.with(context)
					.using((StreamModelLoader<T>)loader)
					.from(clazz)
					.animate(android.R.anim.fade_in)
					.error(R.drawable.inventory_image_error);
			if (DISABLE && BuildConfig.DEBUG) {
				builder = builder
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.skipMemoryCache(true)
				;
			}
			return builder;
		}

		private ResourceDecoder<ImageVideoWrapper, GifBitmapWrapper> getSvgDecoder(Context context) {
			BitmapPool pool = Glide.get(context).getBitmapPool();
			return new GifBitmapWrapperResourceDecoder(
					new ImageVideoBitmapDecoder(
							new SvgBitmapDecoder(pool, new RawResourceSVGExternalFileResolver(context, pool)),
							null
					),
					new GifResourceDecoder(context, pool),
					pool
			);
		}

		public static class GlideSetup implements GlideModule {
			@Override public void applyOptions(final Context context, GlideBuilder builder) {
				builder.setDecodeFormat(PREFERRED_FORMAT);
				if (BuildConfig.DEBUG) {
					builder.setDiskCache(new DiskCache.Factory() {
						@Override public DiskCache build() {
							return DiskLruCacheWrapper.get(getDir(context), 250 * 1024 * 1024);
						}
					});
				}
			}
			@Override public void registerComponents(Context context, Glide glide) {
				// no op
			}

			private static @NonNull File getDir(Context context) {
				return new File(context.getExternalCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR);
			}

			public static @NonNull File getCacheDir(Context context) {
				if (BuildConfig.DEBUG) {
					return getDir(context);
				}
				return Glide.getPhotoCacheDir(context);
			}
		}
	}
}
