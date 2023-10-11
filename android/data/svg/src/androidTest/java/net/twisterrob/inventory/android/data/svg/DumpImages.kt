package net.twisterrob.inventory.android.data.svg;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.model.MultipleFailureException;
import org.slf4j.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build.VERSION;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.SVG;

import androidx.annotation.*;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.io.FileTestStorage;
import androidx.test.platform.io.OutputDirCalculator;
import androidx.test.platform.io.PlatformTestStorage;
import androidx.test.platform.io.PlatformTestStorageRegistry;

import net.twisterrob.java.io.IOTools;
import net.twisterrob.java.utils.ObjectTools;

/**
 * Save all the raw resources as PNG into a folder, ZIP it up and publish to /sdcard.
 * Doesn't work in Robolectric because Canvas is not implemented there.
 */
@RunWith(AndroidJUnit4.class)
public class DumpImages {

	private static final Logger LOG = LoggerFactory.getLogger(DumpImages.class);

	@Test
	public void test() throws Throwable {
		PlatformTestStorage storage = PlatformTestStorageRegistry.getInstance();
		assumeFalse(
				"PlatformTestStorage goes nowhere!",
				"NoOpPlatformTestStorage".equals(storage.getClass().getSimpleName())
		);
		LOG.error("SVG version {}", SVG.getVersion());
		final Context context = ApplicationProvider.getApplicationContext();
		Field[] fields = R.raw.class.getFields();

		File dir = new File(context.getCacheDir(), "svg");
		IOTools.delete(dir);
		IOTools.ensure(dir);
		int cpus = Runtime.getRuntime().availableProcessors();
		LOG.info("Running on {} cores.", cpus);
		ExecutorService service = Executors.newFixedThreadPool(cpus);
		Set<String> exclusions = new HashSet<>(Arrays.asList(
				"shrink_resources", // Resource for AAPT.
				"icon_preview", // Development tool.
				"icon_helpers", // Development tool.
				"icon_template" // Template for new icons.
		));
		List<Future<?>> jobs = new ArrayList<>();
		for (final Field field : fields) {
			if (exclusions.contains(field.getName())) {
				continue;
			}
			final File target = new File(dir, field.getName() + ".png");
			@RawRes final int rawId = (Integer)field.get(null);
			jobs.add(service.submit(new Runnable() {
				@Override public void run() {
					try {
						saveSVG(context, rawId, target);
						LOG.info("Generated {}", target.getAbsolutePath());
					} catch (Exception ex) {
						String fullName = field.getDeclaringClass().getName() + "." + field.getName();
						RuntimeException wrapped = new IllegalStateException("Couldn't generate " + fullName, ex);
						try {
							String stackTrace = ObjectTools.getFullStackTrace(ex);
							IOTools.writeAll(new FileOutputStream(target), stackTrace);
							LOG.warn(wrapped.getMessage(), ex);
						} catch (IOException ex2) {
							// Shouldn't happen normally.
							throw new IllegalStateException("Failed to write exception", ex2);
						}
						throw wrapped;
					}
				}
			}));
		}
		service.shutdown();
		assertTrue(service.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS));
		File zip = new File(dir.getParentFile(), dir.getName() + ".zip");
		IOTools.zip(zip, false, dir);
		LOG.info("ZIP: {}", zip.getAbsolutePath());
		String fileName = "svg_" + VERSION.SDK_INT + ".zip";
		OutputStream saved = storage.openOutputFile(fileName);
		IOTools.copyStream(new FileInputStream(zip), saved);
		if (storage instanceof FileTestStorage) {
			// Guess where storage.openOutputFile() puts the file.
			File target = new File(new OutputDirCalculator().getOutputDir(), fileName);
			LOG.error("adb pull {}", target.getAbsolutePath());
		} else {
			String agpFolder = "build/outputs/(connected|managed_device)_android_test_additional_output/...";
			LOG.error("{} should be available in {}", fileName, agpFolder);
		}
		failTestIfAnyJobFailed(jobs);
	}

	private static void failTestIfAnyJobFailed(List<Future<?>> jobs) throws Exception {
		List<Throwable> errors = new LinkedList<>();
		for (Future<?> job : jobs) {
			try {
				job.get();
			} catch (ExecutionException ex) {
				errors.add(ex.getCause());
			}
		}
		MultipleFailureException.assertEmpty(errors);
	}

	private static void save(@NonNull Bitmap bitmap, @NonNull File file) throws IOException {
		IOTools.ensure(file.getParentFile());
		FileOutputStream stream = new FileOutputStream(file);
		try {
			bitmap.compress(CompressFormat.PNG, 100, stream);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}

	private static void saveSVG(@NonNull Context context, @RawRes int svgRes, @NonNull File file)
			throws IOException {
		LOG.info("Loading {}", context.getResources().getResourceEntryName(svgRes));
		FutureTarget<Bitmap> target = Glide
				.with(context)
				.asBitmap()
				.decode(SVG.class)
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.load(svgRes)
				.submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
		try {
			Bitmap bitmap = target.get();
			save(bitmap, file);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof GlideException) {
				((GlideException)cause).logRootCauses(LOG.getName());
			}
			throw new IOException("Couldn't load Bitmap via Glide, see above for details.", ex);
		} catch (InterruptedException ex) {
			throw new IOException("Couldn't load Bitmap via Glide", ex);
		} finally {
			target.cancel(true);
		}
	}
}
