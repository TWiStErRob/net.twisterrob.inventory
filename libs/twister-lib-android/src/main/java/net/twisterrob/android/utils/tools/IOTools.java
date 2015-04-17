package net.twisterrob.android.utils.tools;

import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.*;
import org.slf4j.*;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import android.os.Build.*;
import android.os.ParcelFileDescriptor;

import net.twisterrob.android.utils.cache.ImageSDNetCache;
import net.twisterrob.java.utils.CollectionTools;

public/* static */class IOTools extends net.twisterrob.java.io.IOTools {
	private static final Logger LOG = LoggerFactory.getLogger(IOTools.class);

	// TODO check if UTF-8 is used by cineworld
	private static final String DEFAULT_HTTP_ENCODING = ENCODING;
	private static final String HTTP_HEADER_CHARSET_PREFIX = "charset=";
	private static ImageSDNetCache imageCache;

	protected IOTools() {
		// prevent instantiation
	}

	public static String getEncoding(final HttpEntity entity) {
		String encoding = DEFAULT_HTTP_ENCODING;
		Header header = entity.getContentEncoding();
		if (header != null) {
			return header.getValue();
		}
		// else
		header = entity.getContentType();
		if (header != null) {
			String value = header.getValue();
			int startIndex = value.indexOf(HTTP_HEADER_CHARSET_PREFIX);
			if (startIndex != -1) {
				startIndex += HTTP_HEADER_CHARSET_PREFIX.length();
				int endIndex = value.indexOf(';', startIndex);
				if (endIndex == -1) {
					endIndex = value.length();
				}
				return value.substring(startIndex, endIndex);
			}
		}
		return encoding;
	}

	@SuppressWarnings("resource")
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

	private static Set<Object> s_getImageLocks =
			CollectionTools.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());

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
	private static synchronized void ensureImageCache() {
		if (imageCache == null) {
			imageCache = new ImageSDNetCache();
		}
	}
	public static Bitmap getImage(final String urlString) throws IOException {
		return getImage(urlString, false);
	}
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
}
