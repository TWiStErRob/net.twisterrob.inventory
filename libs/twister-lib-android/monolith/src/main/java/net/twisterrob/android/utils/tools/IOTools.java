package net.twisterrob.android.utils.tools;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipFile;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.*;
import android.os.Build.*;
import android.os.ParcelFileDescriptor;
import android.system.*;

import androidx.annotation.Nullable;

import net.twisterrob.java.utils.CollectionTools;

@SuppressWarnings("unused")
public /*static*/ abstract class IOTools extends net.twisterrob.java.io.IOTools {
	private static final Logger LOG = LoggerFactory.getLogger(IOTools.class);

	// FIXME check if UTF-8 is used by cineworld
	private static final String DEFAULT_HTTP_ENCODING = ENCODING;
	private static final String HTTP_HEADER_CHARSET_PREFIX = "charset=";
	/** @deprecated use Glide */
	@Deprecated @SuppressWarnings("deprecation")
	private static net.twisterrob.android.utils.cache.ImageSDNetCache imageCache;

	// TODO merge with Cineworld
	//public static String getEncoding(final org.apache.http.HttpEntity entity);

	/** @deprecated use Glide */
	@Deprecated @SuppressWarnings("deprecation")
	public static Bitmap getImage(final URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		InputStream input = null;
		try {
			connection.connect();
			input = connection.getInputStream();

			return BitmapFactory.decodeStream(input);
		} finally {
			closeConnection(connection, input);
		}
	}

	/** @deprecated use Glide */
	@Deprecated @SuppressWarnings("deprecation")
	private static final Set<Object> s_getImageLocks =
			CollectionTools.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());

	/**
	 * @param url will be used for synchronization
	 * @deprecated use Glide
	 */
	@Deprecated @SuppressWarnings("deprecation")
	public static Bitmap getImage(final URL url, boolean cache) throws IOException {
		if (!cache) {
			return getImage(url);
		}
		try {
			ensureImageCache();
			Bitmap result = imageCache.get(url);
			LOG.trace("getImage({}): {}", url, result != null? "hit" : "miss");
			if (result == null) {
				boolean added = s_getImageLocks.add(url);
				if (added) {
					//noinspection SynchronizationOnLocalVariableOrMethodParameter this method is deprecated
					synchronized (url) {
						if (s_getImageLocks.contains(url)) {
							Bitmap newImage = getImage(url, false);
							s_getImageLocks.remove(url);
							imageCache.put(url, newImage);
							LOG.trace("getImage({}): {}", url, "loaded");
						} else {
							LOG.trace("getImage({}): {}", url, "already-loaded");
						}
					}
				}
				result = imageCache.get(url);
				assert result != null : "Image cache is misbehaving";
			}
			return result;
		} catch (Exception ex) {
			throw new IOException("Cannot use cache", ex);
		}
	}
	/** @deprecated use Glide */
	@Deprecated @SuppressWarnings("deprecation")
	private static synchronized void ensureImageCache() {
		if (imageCache == null) {
			imageCache = new net.twisterrob.android.utils.cache.ImageSDNetCache();
		}
	}
	/** @deprecated use Glide */
	@Deprecated @SuppressWarnings("deprecation")
	public static Bitmap getImage(final String urlString) throws IOException {
		return getImage(urlString, false);
	}
	/** @deprecated use Glide */
	@Deprecated @SuppressWarnings("deprecation")
	public static Bitmap getImage(final String urlString, boolean cache) throws IOException {
		URL url = new URL(urlString);
		return IOTools.getImage(url, cache);
	}

	@SuppressWarnings("resource")
	public static String getAssetAsString(Context context, String fileName) {
		InputStream stream = null;
		try {
			stream = context.getAssets().open(fileName, AssetManager.ACCESS_STREAMING);
			return readAll(stream);
		} catch (IOException ex) {
			LOG.warn("Cannot open {}", fileName, ex);
			return null;
		} finally {
			ignorantClose(stream);
		}
	}

	/**
	 * {@link ZipFile} doesn't implement {@link Closeable} before API 19 so we need a specialized method.
	 * @param closeMe more specific than {@link #ignorantClose(Closeable)} won't throw {@link IncompatibleClassChangeError}
	 * @see <a href="https://android.googlesource.com/platform/libcore/+/9902f3494c6d983879d8b9cfe6b1f771cfefe703%5E%21/#F7">Finish off AutoCloseable.</a>
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	public static void ignorantClose(ZipFile closeMe) {
		if (closeMe != null) {
			try {
				closeMe.close();
			} catch (IOException e) {
				LOG.warn("Cannot close " + closeMe, e);
			}
		}
	}

	/**
	 * {@link Cursor} doesn't implement {@link Closeable} before 4.1.1_r1 so we need a specialized method.
	 * @param closeMe more specific than {@link #ignorantClose(Closeable)} won't throw {@link IncompatibleClassChangeError}
	 * @see <a href="https://github.com/android/platform_frameworks_base/commit/03bd302aebbb77f4f95789a269c8a5463ac5a840">Don't close the database until all references released.</a>
	 */
	@TargetApi(VERSION_CODES.JELLY_BEAN)
	public static void ignorantClose(Cursor closeMe) {
		if (closeMe != null) {
			closeMe.close(); // doesn't declare to throw IOException
		}
	}

	// CONSIDER adding more specializations for other (Auto)Closeables 

	/**
	 * {@link ParcelFileDescriptor} doesn't implement {@link Closeable} before 4.1.1_r1 so we need a specialized method.
	 * @param closeMe more specific than {@link #ignorantClose(Closeable)} won't throw {@link IncompatibleClassChangeError}
	 * @see <a href="https://github.com/bumptech/glide/issues/157">ParcelFileDescriptor image loading is broken pre 4.1.1_r1</a>
	 * @see <a href="https://github.com/android/platform_frameworks_base/commit/e861b423790e5bf2d5a55b096065c6ad0541d5bb">Add Closeable to ParcelFileDescriptor, and always close any incoming PFDs when dumping.</a>
	 */
	@TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
	public static void ignorantClose(ParcelFileDescriptor closeMe) {
		if (closeMe != null) {
			try {
				closeMe.close();
			} catch (IOException e) {
				LOG.warn("Cannot close " + closeMe, e);
			}
		}
	}

	@TargetApi(VERSION_CODES.KITKAT)
	public static void closeWithError(ParcelFileDescriptor pfd, String message) throws IOException {
		if (VERSION.SDK_INT < VERSION_CODES.KITKAT) {
			pfd.close();
		} else {
			pfd.closeWithError(message);
		}
	}

	@TargetApi(VERSION_CODES.KITKAT)
	public static void ignorantCloseWithError(ParcelFileDescriptor pfd, String message) {
		try {
			closeWithError(pfd, message);
		} catch (IOException e) {
			LOG.warn("Cannot close " + pfd + " with error: " + message, e);
		}
	}

	protected IOTools() {
		// prevent instantiation
	}

	@TargetApi(VERSION_CODES.LOLLIPOP)
	public static boolean isEPIPE(@Nullable Throwable ex) {
		if (ex == null) {
			return false;
		}
		int code = -1;
		if (ex instanceof IOException) {
			ex = ex.getCause();
		}
		if (VERSION_CODES.LOLLIPOP <= VERSION.SDK_INT && ex instanceof ErrnoException) {
			code = ((ErrnoException)ex).errno;
		} else if ("ErrnoException".equals(ex.getClass().getSimpleName())) {
			// before 21 it's libcore.io.ErrnoException
			try {
				Field errno = ex.getClass().getDeclaredField("errno");
				code = (Integer)errno.get(ex);
			} catch (Throwable ignore) {
				// don't bother, we're doing best effort
			}
		}
		int epipe = VERSION_CODES.LOLLIPOP <= VERSION.SDK_INT? OsConstants.EPIPE : 32 /* from errno.h */;
		return code == epipe || (ex.getMessage() != null && ex.getMessage().contains("EPIPE"));
	}
}
