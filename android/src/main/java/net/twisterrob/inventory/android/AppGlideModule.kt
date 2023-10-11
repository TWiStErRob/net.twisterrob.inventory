package net.twisterrob.inventory.android

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import net.twisterrob.inventory.android.Constants.DISABLE
import net.twisterrob.inventory.android.base.BuildConfig

@GlideModule
class AppGlideModule : AppGlideModule() {

	// REPORT Glide is this called?
	override fun isManifestParsingEnabled(): Boolean = false

	override fun applyOptions(context: Context, builder: GlideBuilder) {
		if (BuildConfig.DEBUG) {
			if (DISABLE) {
				builder.setDiskCache(ExternalPreferredCacheDiskCacheFactory(context))
			}
			builder.setLogLevel(Log.VERBOSE)
		}
	}
}
