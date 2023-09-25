package net.twisterrob.inventory.android;

import java.io.*;
import java.util.*;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import androidx.annotation.NonNull;
import androidx.core.content.*;
import androidx.core.graphics.ColorUtils;

import net.twisterrob.android.content.glide.*;
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

		public static @NonNull String getExportFileName(Calendar now) {
			return getFileName("Inventory", now, "zip");
		}
		public static @NonNull String getFileName(String prefix, Calendar now, String extension) {
			return String.format(Locale.ROOT, "%s_%tF_%<tH-%<tM-%<tS.%s", prefix, now, extension);
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
		private final @NonNull ColorFilter ghostFilter;
		/**
		 * Color somewhere between accent and accentDark.
		 * This means that it'll be visible even if those are used as a background where this tint is applied.
		 * There's no point in changing accentDark to this mixture, because then selection highlight won't work nicely. 
		 */
		private final @NonNull ColorFilter tintFilter;

		private final @NonNull RequestBuilder<Drawable> imageRequest;
		private final @NonNull RequestBuilder<Drawable> svgRequest;
		private final @NonNull RequestBuilder<Drawable> svgRequestTinted;

		private Pic(@NonNull Context context, @NonNull String versionName) {
			context = context.getApplicationContext();
			RequestBuilder<Drawable> baseSvgRequest = baseRequest(Integer.class, context)
					.dontAnimate()
					.signature(new ObjectKey(versionName))
					.priority(Priority.HIGH)
					// STOPSHIP .decoder(getSvgDecoder(context))
					;
			if (DISABLE && BuildConfig.DEBUG) {
				// STOPSHIP use formatter
				LoggingListener.ModelFormatter<Integer> formatter =
						LoggingListener.ModelFormatter.Companion.forResources(context);
				baseSvgRequest = baseSvgRequest
						.listener(new LoggingListener<Drawable>("SVG"));
			}
			ghostFilter = createGhostFilter(context);
			tintFilter = createTintFilter(context);

			svgRequest = baseSvgRequest
					.clone()
					/* STOPSHIP how to migrate this?
					.transcoder(new GifBitmapWrapperDrawableTranscoder(
							new FilteredGlideBitmapDrawableTranscoder(context, "primary-ghost", ghostFilter)
					))*/
					;
			svgRequestTinted = baseSvgRequest
					.clone()
					/* STOPSHIP how to migrate this?
					.transcoder(new GifBitmapWrapperDrawableTranscoder(
							new FilteredGlideBitmapDrawableTranscoder(context, "accent-tint", tintFilter)
					))*/
					;

			RequestBuilder<Drawable> imageRequest;
			imageRequest = baseRequest(Uri.class, context)
					.transition(GenericTransitionOptions.with(android.R.anim.fade_in))
					.priority(Priority.NORMAL);
			if (DISABLE && BuildConfig.DEBUG) {
				imageRequest = imageRequest.listener(new LoggingListener<Drawable>("image"));
			}
			this.imageRequest = imageRequest;
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

		public static @NonNull RequestBuilder<Drawable> svg() {
			return instance.svgRequestTinted.clone();
		}
		public static @NonNull RequestBuilder<Drawable> svgNoTint() {
			return instance.svgRequest.clone();
		}
		public static @NonNull RequestBuilder<Drawable> jpg() {
			return instance.imageRequest.clone();
		}
		public static @NonNull ColorFilter tint() {
			return instance.tintFilter;
		}
		public static void init(@NonNull Context context, @NonNull String versionName) {
			instance = new Pic(context, versionName);
		}

		private static <T> RequestBuilder<Drawable> baseRequest(@NonNull Class<T> clazz, @NonNull Context context) {
			// STOPSHIP ModelLoader<T, InputStream> loader = Glide.buildModelLoader(clazz, InputStream.class, context);
			// FIXME replace this with proper Glide.with calls, don't use App Context
			RequestBuilder<Drawable> builder = Glide
					.with(context)
					.asDrawable()
		// STOPSHIP .using((StreamModelLoader<T>)loader)
		// STOPSHIP .from(clazz)
					.transition(GenericTransitionOptions.with(android.R.anim.fade_in))
					.error(R.drawable.inventory_image_error);
			if (DISABLE && BuildConfig.DEBUG) {
				builder = builder
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.skipMemoryCache(true)
				;
			}
			return builder;
		}
/* STOPSHIP how to migrate this?
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
*/
		@GlideModule
		public static class GlideSetup extends AppGlideModule {
			@Override
			public boolean isManifestParsingEnabled() {
				return false;
			}

			@Override public void applyOptions(final @NonNull Context context, @NonNull GlideBuilder builder) {
				// STOPSHIP this is the default now:
				builder.setDefaultRequestOptions(new RequestOptions().format(PREFERRED_FORMAT));
				if (BuildConfig.DEBUG) {
					builder.setDiskCache(new DiskCache.Factory() {
						@Override public DiskCache build() {
							return DiskLruCacheWrapper.create(getDir(context), 250 * 1024 * 1024);
						}
					});
				}
			}

			private static @NonNull File getDir(@NonNull Context context) {
				return new File(context.getExternalCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR);
			}

			public static @NonNull File getCacheDir(@NonNull Context context) {
				if (BuildConfig.DEBUG) {
					return getDir(context);
				}
				// STOPSHIP can be null, can we get the real cache dir, i.e. compatible with DEBUG above?
				return Glide.getPhotoCacheDir(context);
			}
		}
	}
}
