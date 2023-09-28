package net.twisterrob.inventory.android

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import net.twisterrob.inventory.android.Constants.Pic
import net.twisterrob.inventory.android.base.BuildConfig

@GlideModule
class AppGlideModule : AppGlideModule() {

	// REPORT is this called?
	override fun isManifestParsingEnabled(): Boolean = false

	override fun applyOptions(context: Context, builder: GlideBuilder) {
		// STOPSHIP this is the default now:
		builder.setDefaultRequestOptions(RequestOptions().format(Pic.PREFERRED_FORMAT))
		if (BuildConfig.DEBUG) {
			builder.setDiskCache {
				DiskLruCacheWrapper.create(Pic.getDir(context), 250.MB)
			}
		}
	}
}

@Suppress("PrivatePropertyName")
private val Int.MB: Long
	get() = this.toLong() * 1024 * 1024
