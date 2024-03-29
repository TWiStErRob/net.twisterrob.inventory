package net.twisterrob.inventory.android.data.svg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule
import com.caverock.androidsvg.SVG
import net.twisterrob.android.content.glide.svg.RawResourceSVGExternalFileResolver
import net.twisterrob.android.content.glide.svg.SvgAutoSizeTransformation
import net.twisterrob.android.content.glide.svg.SvgBitmapDrawableTranscoder
import net.twisterrob.android.content.glide.svg.SvgBitmapTranscoder
import net.twisterrob.android.content.glide.svg.SvgDecoder
import java.io.InputStream

@GlideModule
class SvgLibraryModule : LibraryGlideModule() {
	override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
		registry.append(InputStream::class.java, SVG::class.java, SvgDecoder(context, SvgAutoSizeTransformation()))
		registry.register(SVG::class.java, Bitmap::class.java, SvgBitmapTranscoder(glide.bitmapPool))
		registry.register(SVG::class.java, BitmapDrawable::class.java, SvgBitmapDrawableTranscoder(context.resources, SvgBitmapTranscoder(glide.bitmapPool)))
		//registry.register(SVG::class.java, PictureDrawable::class.java, SvgPictureDrawableTranscoder())
		SVG.registerExternalFileResolver(RawResourceSVGExternalFileResolver(context, glide.bitmapPool))
	}
}
