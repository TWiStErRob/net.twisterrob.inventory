package net.twisterrob.inventory.android.data.svg

import android.content.Context
import android.graphics.Bitmap
import android.os.Build.VERSION
import androidx.annotation.RawRes
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.io.FileTestStorage
import androidx.test.platform.io.OutputDirCalculator
import androidx.test.platform.io.PlatformTestStorageRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.caverock.androidsvg.SVG
import net.twisterrob.java.io.IOTools
import net.twisterrob.java.utils.ObjectTools
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.model.MultipleFailureException
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

private val LOG = LoggerFactory.getLogger(DumpImages::class.java)

/**
 * Save all the raw resources as PNG into a folder, ZIP it up and publish to /sdcard.
 * Doesn't work in Robolectric because Canvas is not implemented there.
 */
@RunWith(AndroidJUnit4::class)
class DumpImages {

	@Test fun test() {
		val storage = PlatformTestStorageRegistry.getInstance()
		assumeFalse(
			"PlatformTestStorage goes nowhere!",
			"NoOpPlatformTestStorage" == storage.javaClass.simpleName
		)
		LOG.error("SVG version {}", SVG.getVersion())
		val context: Context = ApplicationProvider.getApplicationContext()
		val fields = R.raw::class.java.fields
		val dir = context.cacheDir.resolve("svg")
		IOTools.delete(dir)
		IOTools.ensure(dir)
		val service = createExecutor()
		val jobs = fields.mapNotNull { field ->
			if (exclusions.contains(field.name)) {
				return@mapNotNull null
			}
			val target = dir.resolve("${field.name}.png")
			@RawRes val rawId = field.get(null) as Int
			service.submit {
				try {
					saveSVG(context, rawId, target)
					LOG.info("Generated {}", target.absolutePath)
				} catch (ex: Exception) {
					throw saveError(field, ex, target)
				}
			}
		}
		service.shutdown()
		assertTrue(service.awaitTermination(5, TimeUnit.MINUTES))
		val zip = dir.resolveSibling("${dir.name}.zip")
		IOTools.zip(zip, false, dir)
		LOG.info("ZIP: {}", zip.absolutePath)
		val fileName = "svg_${VERSION.SDK_INT}.zip"
		IOTools.copyStream(zip.inputStream(), storage.openOutputFile(fileName))
		if (storage is FileTestStorage) {
			// Guess where storage.openOutputFile() puts the file.
			val target = OutputDirCalculator().outputDir.resolve(fileName)
			LOG.error("adb pull {}", target.absolutePath)
		} else {
			val agpFolder = "build/outputs/(connected|managed_device)_android_test_additional_output/..."
			LOG.error("{} should be available in {}", fileName, agpFolder)
		}
		failTestIfAnyJobFailed(jobs)
	}

	companion object {

		private val exclusions: Set<String> = setOf(
			"shrink_resources",  // Resource for AAPT.
			"icon_preview",  // Development tool.
			"icon_helpers",  // Development tool.
			"icon_template" // Template for new icons.
		)

		private fun createExecutor(): ExecutorService {
			val cpus = Runtime.getRuntime().availableProcessors()
			LOG.info("Running on {} cores.", cpus)
			return Executors.newFixedThreadPool(cpus)
		}

		private fun saveError(field: Field, ex: Exception, target: File): IllegalStateException {
			val fullName = "${field.declaringClass.name}.${field.name}"
			val wrapped = IllegalStateException("Couldn't generate ${fullName}", ex)
			try {
				val stackTrace = ObjectTools.getFullStackTrace(ex)!!
				IOTools.writeAll(target.outputStream(), stackTrace)
				LOG.warn(wrapped.message, ex)
			} catch (ex2: IOException) {
				// Shouldn't happen normally.
				throw IllegalStateException("Failed to write exception", ex2)
			}
			return wrapped
		}

		@Throws(IOException::class)
		private fun saveSVG(context: Context, @RawRes svgRes: Int, file: File) {
			LOG.info("Loading {}", context.resources.getResourceEntryName(svgRes))
			val target = Glide
				.with(context)
				.asBitmap()
				.decode(SVG::class.java)
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.load(svgRes)
				.submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
			try {
				target.get().save(file)
			} catch (ex: ExecutionException) {
				val cause = ex.cause
				if (cause is GlideException) {
					cause.logRootCauses(LOG.name)
				}
				throw IOException("Couldn't load Bitmap via Glide, see above for details.", ex)
			} catch (ex: InterruptedException) {
				throw IOException("Couldn't load Bitmap via Glide", ex)
			} finally {
				target.cancel(true)
			}
		}

		@Throws(Exception::class)
		private fun failTestIfAnyJobFailed(jobs: Iterable<Future<*>>) {
			MultipleFailureException.assertEmpty(jobs.getErrors())
		}
	}
}

@Throws(IOException::class)
private fun Bitmap.save(file: File) {
	IOTools.ensure(file.parentFile!!)
	file.outputStream().use {
		this.compress(Bitmap.CompressFormat.PNG, 100, it)
	}
}

private fun Iterable<Future<*>>.getErrors(): List<Throwable> =
	this
		.map { runCatching { it.get(1, TimeUnit.MINUTES) } }
		.filter { it.isFailure }
		.map { it.exceptionOrNull()!! }
		.map { (it as? ExecutionException)?.cause ?: it }
