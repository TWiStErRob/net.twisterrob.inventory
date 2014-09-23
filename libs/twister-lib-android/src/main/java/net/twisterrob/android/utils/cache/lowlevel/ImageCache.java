/* #noformat
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// #endnoformat

package net.twisterrob.android.utils.cache.lowlevel;

import java.io.*;
import java.security.*;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.util.LruCache;
import android.util.Log;

import net.twisterrob.java.io.IOTools;

/**
 * This class holds our bitmap caches (memory and disk).
 */
public class ImageCache {
	private static final String TAG = "ImageCache";

	// Default memory cache size
	protected static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 5; // 5MB

	// Default disk cache size
	protected static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

	// Compression settings when writing images to disk cache
	protected static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
	protected static final int DEFAULT_COMPRESS_QUALITY = 70;
	private static final int DISK_CACHE_INDEX = 0;
	protected static final boolean DEFAULT_LOG_CACHE_LIFECYCLE = false;

	// Constants to easily toggle various caches
	protected static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
	protected static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
	protected static final boolean DEFAULT_CLEAR_DISK_CACHE_ON_START = false;
	protected static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

	private DiskLruCache mDiskLruCache;
	private LruCache<String, Bitmap> mMemoryCache;
	private ImageCacheParams mCacheParams;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;

	/**
	 * Creating a new ImageCache object using the specified parameters.
	 * 
	 * @param cacheParams The cache parameters to use to initialize the cache
	 */
	public ImageCache(final ImageCacheParams cacheParams) {
		init(cacheParams);
	}

	/**
	 * Creating a new ImageCache object using the default parameters.
	 * 
	 * @param context The context to use
	 * @param uniqueName A unique name that will be appended to the cache directory
	 */
	public ImageCache(final Context context, final String uniqueName) {
		init(new ImageCacheParams(context, uniqueName));
	}

	/**
	 * Find and return an existing ImageCache stored in a {@link RetainFragment}, if not found a new one is created
	 * using the supplied params and saved to a {@link RetainFragment}.
	 * 
	 * @param fragmentManager The fragment manager to use when dealing with the retained fragment.
	 * @param cacheParams The cache parameters to use if creating the ImageCache
	 * @return An existing retained ImageCache object or a new one if one did not exist
	 */
	public static ImageCache findOrCreateCache(final FragmentManager fragmentManager, final ImageCacheParams cacheParams) {

		// Search for, or create an instance of the non-UI RetainFragment
		final RetainFragment mRetainFragment = ImageCache.findOrCreateRetainFragment(fragmentManager);

		// See if we already have an ImageCache stored in RetainFragment
		ImageCache imageCache = (ImageCache)mRetainFragment.getObject();

		// No existing ImageCache, create one and store it in RetainFragment
		if (imageCache == null) {
			imageCache = new ImageCache(cacheParams);
			mRetainFragment.setObject(imageCache);
		}

		return imageCache;
	}

	/**
	 * Initialize the cache, providing all parameters.
	 * 
	 * @param cacheParams The cache parameters to initialize the cache
	 */
	private void init(final ImageCacheParams cacheParams) {
		mCacheParams = cacheParams;

		// Set up memory cache
		if (mCacheParams.memoryCacheEnabled) {
			if (mCacheParams.logCacheLifecycle) {
				Log.d(TAG, "Memory cache created (size = " + mCacheParams.memoryCacheSize + ")");
			}
			mMemoryCache = new LruCache<String, Bitmap>(mCacheParams.memoryCacheSize) {
				/**
				 * Measure item size in bytes rather than units which is more practical for a bitmap cache
				 */
				@Override
				protected int sizeOf(final String key, final Bitmap bitmap) {
					return ImageCache.getBitmapSize(bitmap);
				}
			};
		}

		// By default the disk cache is not initialized here as it should be initialized
		// on a separate thread due to disk access.
		if (cacheParams.initDiskCacheOnCreate) {
			// Set up disk cache
			initDiskCache();
		}
	}

	/**
	 * Initializes the disk cache. Note that this includes disk access so this should not be executed on the main/UI
	 * thread. By default an ImageCache does not initialize the disk cache when it is created, instead you should call
	 * initDiskCache() to initialize it on a background thread.
	 */
	public void initDiskCache() {
		// Set up disk cache
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
				File diskCacheDir = mCacheParams.diskCacheDir;
				if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
					if (!diskCacheDir.exists()) {
						diskCacheDir.mkdirs();
					}
					if (ImageCache.getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
						try {
							mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
							if (mCacheParams.logCacheLifecycle) {
								Log.d(TAG, "Disk cache initialized");
							}
						} catch (final IOException e) {
							mCacheParams.diskCacheDir = null;
							Log.e(TAG, "initDiskCache - " + e);
						}
					}
				}
			}
			mDiskCacheStarting = false;
			mDiskCacheLock.notifyAll();
		}
	}

	/**
	 * Adds a bitmap to both memory and disk cache.
	 * 
	 * @param data Unique identifier for the bitmap to store
	 * @param bitmap The bitmap to store
	 */
	public void addBitmapToCache(final String data, final Bitmap bitmap) {
		if (data == null || bitmap == null) {
			return;
		}

		// Add to memory cache
		if (mMemoryCache != null && mMemoryCache.get(data) == null) {
			mMemoryCache.put(data, bitmap);
		}

		synchronized (mDiskCacheLock) {
			// Add to disk cache
			if (mDiskLruCache != null) {
				final String key = ImageCache.hashKeyForDisk(data);
				OutputStream out = null;
				try {
					@SuppressWarnings("resource")
					DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot == null) {
						final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							bitmap.compress(getCompressFormat(data, bitmap, mCacheParams),
									mCacheParams.compressQuality, out);
							editor.commit();
							out.close();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (final IOException e) {
					Log.e(TAG, "addBitmapToCache - " + e);
				} catch (Exception e) {
					Log.e(TAG, "addBitmapToCache - " + e);
				} finally {
					IOTools.ignorantClose(out);
				}
			}
		}
	}

	/**
	 * @param bitmap no need for now
	 */
	private static CompressFormat getCompressFormat(String data, Bitmap bitmap, ImageCacheParams params) {
		if (params.forceCompressFormat) {
			return params.compressFormat;
		}
		String extension = data.replaceFirst("^[^#]*/[^#]*(?:\\.([^#]*)(?:#.*)?)$", "$1");
		if (extension.matches("(?i:jpe?g|jpe)")) {
			return CompressFormat.JPEG;
		} else if (extension.matches("(?i:png)")) {
			return CompressFormat.PNG;
		} else {
			return params.compressFormat;
		}
	}
	/**
	 * Get from memory cache.
	 * 
	 * @param data Unique identifier for which item to get
	 * @return The bitmap if found in cache, null otherwise
	 */
	public Bitmap getBitmapFromMemCache(final String data) {
		if (mMemoryCache != null) {
			final Bitmap memBitmap = mMemoryCache.get(data);
			if (memBitmap != null) {
				if (mCacheParams.logCacheLifecycle) {
					Log.d(TAG, String.format("Memory cache hit for %s", data));
				}
				return memBitmap;
			}
		}
		return null;
	}

	/**
	 * Get from disk cache.
	 * 
	 * @param data Unique identifier for which item to get
	 * @return The bitmap if found in cache, null otherwise
	 */
	@SuppressWarnings("resource")
	public Bitmap getBitmapFromDiskCache(final String data) {
		final String key = ImageCache.hashKeyForDisk(data);
		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
					// TODO ignore
				}
			}
			if (mDiskLruCache != null) {
				InputStream inputStream = null;
				try {
					final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot != null) {
						if (mCacheParams.logCacheLifecycle) {
							Log.d(TAG, String.format("Disk cache hit for %s: %s", data,
									snapshot.getFile(DISK_CACHE_INDEX)));
						}
						inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (inputStream != null) {
							final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
							return bitmap;
						}
					}
				} catch (final IOException e) {
					Log.e(TAG, "getBitmapFromDiskCache - " + e);
				} finally {
					IOTools.ignorantClose(inputStream);
				}
			}
			return null;
		}
	}

	/**
	 * Clears both the memory and disk cache associated with this ImageCache object. Note that this includes disk access
	 * so this should not be executed on the main/UI thread.
	 */
	public void clearCache() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
			if (mCacheParams.logCacheLifecycle) {
				Log.d(TAG, "Memory cache cleared");
			}
		}

		synchronized (mDiskCacheLock) {
			mDiskCacheStarting = true;
			if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
				try {
					mDiskLruCache.delete();
					if (mCacheParams.logCacheLifecycle) {
						Log.d(TAG, "Disk cache cleared");
					}
				} catch (IOException e) {
					Log.e(TAG, "clearCache - " + e);
				}
				mDiskLruCache = null;
				initDiskCache();
			}
		}
	}

	/**
	 * Flushes the disk cache associated with this ImageCache object. Note that this includes disk access so this should
	 * not be executed on the main/UI thread.
	 */
	public void flush() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					mDiskLruCache.flush();
					if (mCacheParams.logCacheLifecycle) {
						Log.d(TAG, "Disk cache flushed");
					}
				} catch (IOException e) {
					Log.e(TAG, "flush - " + e);
				}
			}
		}
	}

	/**
	 * Closes the disk cache associated with this ImageCache object. Note that this includes disk access so this should
	 * not be executed on the main/UI thread.
	 */
	public void close() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					if (!mDiskLruCache.isClosed()) {
						mDiskLruCache.close();
						mDiskLruCache = null;
						if (mCacheParams.logCacheLifecycle) {
							Log.d(TAG, "Disk cache closed");
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "close - " + e);
				}
			}
		}
	}

	/**
	 * A holder class that contains cache parameters.
	 */
	public static class ImageCacheParams {
		public boolean forceCompressFormat = false;
		public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
		public int compressQuality = DEFAULT_COMPRESS_QUALITY;
		public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
		public int memoryCacheSize = DEFAULT_MEM_CACHE_SIZE;
		public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
		public File diskCacheDir;
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		public boolean clearDiskCacheOnStart = DEFAULT_CLEAR_DISK_CACHE_ON_START;
		public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;
		public boolean logCacheLifecycle = DEFAULT_LOG_CACHE_LIFECYCLE;

		public ImageCacheParams(final Context context, final String uniqueName) {
			diskCacheDir = ImageCache.getDiskCacheDir(context, uniqueName);
		}

		public ImageCacheParams(final File diskCacheDir) {
			this.diskCacheDir = diskCacheDir;
		}

		/**
		 * Sets the memory cache size based on a percentage of the device memory class. Eg. setting percent to 0.2 would
		 * set the memory cache to one fifth of the device memory class. Throws {@link IllegalArgumentException} if
		 * percent is < 0.05 or > .8. This value should be chosen carefully based on a number of factors Refer to the
		 * corresponding Android Training class for more discussion:
		 * http://developer.android.com/training/displaying-bitmaps/
		 * 
		 * @param context Context to use to fetch memory class
		 * @param percent Percent of memory class to use to size memory cache
		 */
		public void setMemCacheSizePercent(final Context context, final float percent) {
			if (percent < 0.05f || percent > 0.8f) {
				throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
						+ "between 0.05 and 0.8 (inclusive)");
			}
			memoryCacheSize = Math.round(percent * ImageCacheParams.getMemoryClass(context) * 1024 * 1024);
		}

		private static int getMemoryClass(final Context context) {
			return ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		}
	}

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 * 
	 * @param context The context to use
	 * @param uniqueName A unique directory name to append to the cache dir
	 * @return The cache dir
	 */
	public static File getDiskCacheDir(final Context context, final String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use external cache dir
		// otherwise use internal cache dir
		boolean externalOK = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !ImageCache.isExternalStorageRemovable();
		final String cachePath = externalOK? ImageCache.getExternalCacheDir(context).getPath() : context.getCacheDir()
				.getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * A hashing method that changes a string (like a URL) into a hash suitable for using as a disk filename.
	 */
	public static String hashKeyForDisk(final String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = ImageCache.bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(final byte[] bytes) {
		// http://stackoverflow.com/questions/332079
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * Get the size in bytes of a bitmap.
	 * 
	 * @param bitmap
	 * @return size in bytes
	 */
	@TargetApi(VERSION_CODES.HONEYCOMB_MR1)
	public static int getBitmapSize(final Bitmap bitmap) {
		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB_MR1) {
			return bitmap.getHeight() * bitmap.getRowBytes();
		} else {
			return bitmap.getByteCount();
		}
	}

	/**
	 * Check if external storage is built-in or removable.
	 * 
	 * @return True if external storage is removable (like an SD card), false otherwise.
	 */
	@TargetApi(VERSION_CODES.GINGERBREAD)
	public static boolean isExternalStorageRemovable() {
		if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
			return true;
		} else {
			return Environment.isExternalStorageRemovable();
		}
	}

	/**
	 * Get the external app cache directory.
	 * 
	 * @param context The context to use
	 * @return The external cache dir
	 */
	@TargetApi(VERSION_CODES.FROYO)
	public static File getExternalCacheDir(final Context context) {
		if (VERSION.SDK_INT < VERSION_CODES.FROYO) {
			// Before Froyo we need to construct the external cache dir ourselves
			final String cacheDir = "Android/data/" + context.getPackageName() + "/cache";
			return new File(Environment.getExternalStorageDirectory().getPath(), cacheDir);
		} else {
			return context.getExternalCacheDir();
		}
	}

	/**
	 * Check how much usable space is available at a given path.
	 * 
	 * @param path The path to check
	 * @return The space available in bytes
	 */
	@TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
	public static long getUsableSpace(final File path) {
		if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
			StatFs stats = new StatFs(path.getPath());
			return stats.getBlockSize() * stats.getAvailableBlocks();
		} else if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
			return path.getUsableSpace();
		} else { // JELLY_BEAN_MR2 <= SDK_INT
			StatFs stats = new StatFs(path.getPath());
			return stats.getBlockSizeLong() * stats.getAvailableBlocksLong();
		}
	}

	/**
	 * Locate an existing instance of this Fragment or if not found, create and add it using FragmentManager.
	 * 
	 * @param fm The FragmentManager manager to use.
	 * @return The existing instance of the Fragment or the new instance if just created.
	 */
	public static RetainFragment findOrCreateRetainFragment(final FragmentManager fm) {
		// Check to see if we have retained the worker fragment.
		RetainFragment mRetainFragment = (RetainFragment)fm.findFragmentByTag(TAG);

		// If not retained (or first time running), we need to create and add it.
		if (mRetainFragment == null) {
			mRetainFragment = new RetainFragment();
			fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
		}

		return mRetainFragment;
	}

	/**
	 * A simple non-UI Fragment that stores a single Object and is retained over configuration changes. It will be used
	 * to retain the ImageCache object.
	 */
	public static class RetainFragment extends Fragment {
		private Object mObject;

		/**
		 * Empty constructor as per the Fragment documentation
		 */
		public RetainFragment() {}

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Make sure this Fragment is retained over a configuration change
			setRetainInstance(true);
		}

		/**
		 * Store a single object in this Fragment.
		 * 
		 * @param object The object to store
		 */
		public void setObject(final Object object) {
			mObject = object;
		}

		/**
		 * Get the stored object.
		 * 
		 * @return The stored object
		 */
		public Object getObject() {
			return mObject;
		}
	}

}
