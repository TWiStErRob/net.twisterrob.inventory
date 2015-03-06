package net.twisterrob.android.content.glide;

import java.io.File;

import android.content.Context;

import com.bumptech.glide.load.engine.cache.*;

public final class ExternalCacheDiskCacheFactory implements DiskCache.Factory {
	private final Context context;
	private final String diskCacheName;
	private final int diskCacheSize;

	public ExternalCacheDiskCacheFactory(Context context, int diskCacheSize) {
		this(context, null /*diskCacheName*/, diskCacheSize);
	}

	public ExternalCacheDiskCacheFactory(Context context, String diskCacheName, int diskCacheSize) {
		this.context = context;
		this.diskCacheName = diskCacheName;
		this.diskCacheSize = diskCacheSize;
	}

	@Override
	public DiskCache build() {
		DiskCache diskCache = null;
		final File cacheDir;

		if (diskCacheName != null) {
			cacheDir = new File(context.getExternalCacheDir(), diskCacheName);
		} else {
			cacheDir = context.getExternalCacheDir();
		}

		if (cacheDir != null) {
			diskCache = DiskLruCacheWrapper.get(cacheDir, diskCacheSize);
		}

		if (diskCache == null) {
			diskCache = new DiskCacheAdapter();
		}
		return diskCache;
	}
}
