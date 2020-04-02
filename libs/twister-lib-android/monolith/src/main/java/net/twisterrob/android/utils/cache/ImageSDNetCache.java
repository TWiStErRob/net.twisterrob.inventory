package net.twisterrob.android.utils.cache;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;

import net.twisterrob.android.utils.log.*;
import net.twisterrob.android.utils.tools.IOTools;

/** @deprecated use Glide */
@Deprecated @SuppressWarnings("deprecation")
public class ImageSDNetCache implements Cache<URL, Bitmap> {
	private static final Log LOG = LogFactory.getLog(Tag.IO);
	private final net.twisterrob.android.utils.cache.lowlevel.ImageCache m_cache;

	public ImageSDNetCache() {
		Context context = net.twisterrob.android.utils.LibContextProvider.getApplicationContext();
		net.twisterrob.android.utils.cache.lowlevel.ImageCache.ImageCacheParams params =
				new net.twisterrob.android.utils.cache.lowlevel.ImageCache.ImageCacheParams(context, "");
		params.initDiskCacheOnCreate = true;
		params.memoryCacheEnabled = false;
		m_cache = new net.twisterrob.android.utils.cache.lowlevel.ImageCache(params);
	}

	public Bitmap get(final URL key) throws IOException {
		if (key == null) {
			return null;
		}
		Bitmap bitmap = m_cache.getBitmapFromDiskCache(key.toString());
		if (bitmap == null) {
			bitmap = getImage(key);
			put(key, bitmap);
		}
		return bitmap;
	}

	private static Bitmap getImage(final URL key) throws IOException {
		try {
			return IOTools.getImage(key);
		} catch (IOException ex) {
			IOException newEx = new IOException(String.format(Locale.ROOT, "Cannot get image: %s", key), ex);
			LOG.warn(newEx.getMessage(), ex);
			throw newEx;
		}
	}

	public void put(final URL url, final Bitmap image) {
		if (url != null && image != null) {
			m_cache.addBitmapToCache(url.toString(), image);
		}
	}
}
