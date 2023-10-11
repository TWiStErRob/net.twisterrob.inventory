package net.twisterrob.inventory.android.data.svg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.PNG
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.LinkedList
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.DAYS

private val LOG = LoggerFactory.getLogger(DumpImages::class.java)

/**
 * Save all the raw resources as PNG into a folder, ZIP it up and publish to /sdcard.
 * Doesn't work in Robolectric because Canvas is not implemented there.
 */
@RunWith(AndroidJUnit4::class)
class DumpImages {
	@Throws(Throwable::class)
	@Test fun test() {
		val storage = PlatformTestStorageRegistry.getInstance()
		assumeFalse(
			"PlatformTestStorage goes nowhere!",
			"NoOpPlatformTestStorage" == storage.javaClass.simpleName
		)
		LOG.error("SVG version {}", SVG.getVersion())
		val context = ApplicationProvider.getApplicationContext<Context>()
		val fields = R.raw::class.java.fields
		val dir = File(context.cacheDir, "svg")
		IOTools.delete(dir)
		IOTools.ensure(dir)
		val cpus = Runtime.getRuntime().availableProcessors()
		LOG.info("Running on {} cores.", cpus)
		val service = Executors.newFixedThreadPool(cpus)
		val exclusions = setOf(
			"shrink_resources",  // Resource for AAPT.
			"icon_preview",  // Development tool.
			"icon_helpers",  // Development tool.
			"icon_template" // Template for new icons.
		)
		val jobs = mutableListOf<Future<*>>()
		for (field in fields) {
			if (exclusions.contains(field.name)) {
				continue
			}
			val target = File(dir, "${field.name}.png")
			@RawRes val rawId = field[null] as Int
			jobs.add(service.submit {
				try {
					saveSVG(context, rawId, target)
					LOG.info("Generated {}", target.absolutePath)
				} catch (ex: Exception) {
					val fullName = "${field.declaringClass.name}.${field.name}"
					val wrapped: RuntimeException =
						IllegalStateException("Couldn't generate ${fullName}", ex)
					try {
						val stackTrace = ObjectTools.getFullStackTrace(ex)!!
						IOTools.writeAll(FileOutputStream(target), stackTrace)
						LOG.warn(wrapped.message, ex)
					} catch (ex2: IOException) {
						// Shouldn't happen normally.
						throw IllegalStateException("Failed to write exception", ex2)
					}
					throw wrapped
				}
			})
		}
		service.shutdown()
		assertTrue(service.awaitTermination(Int.MAX_VALUE.toLong(), DAYS))
		val zip = File(dir.parentFile, "${dir.name}.zip")
		IOTools.zip(zip, false, dir)
		LOG.info("ZIP: {}", zip.absolutePath)
		val fileName = "svg_${VERSION.SDK_INT}.zip"
		val saved = storage.openOutputFile(fileName)
		IOTools.copyStream(FileInputStream(zip), saved)
		if (storage is FileTestStorage) {
			// Guess where storage.openOutputFile() puts the file.
			val target = File(OutputDirCalculator().outputDir, fileName)
			LOG.error("adb pull {}", target.absolutePath)
		} else {
			val agpFolder =
				"build/outputs/(connected|managed_device)_android_test_additional_output/..."
			LOG.error("{} should be available in {}", fileName, agpFolder)
		}
		failTestIfAnyJobFailed(jobs)
	}

	companion object {
		@Throws(Exception::class)
		private fun failTestIfAnyJobFailed(jobs: Iterable<Future<*>>) {
			val errors: MutableList<Throwable?> = LinkedList()
			for (job in jobs) {
				try {
					job.get()
				} catch (ex: ExecutionException) {
					errors.add(ex.cause)
				}
			}
			MultipleFailureException.assertEmpty(errors)
		}

		@Throws(IOException::class)
		private fun save(bitmap: Bitmap, file: File) {
			IOTools.ensure(file.parentFile!!)
			val stream = FileOutputStream(file)
			try {
				bitmap.compress(PNG, 100, stream)
			} finally {
				IOTools.ignorantClose(stream)
			}
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
				val bitmap = target.get()
				save(bitmap, file)
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
	}
}
