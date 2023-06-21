package net.twisterrob.inventory.android.data.svg;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.*;

import static org.junit.Assume.assumeTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build.VERSION;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.model.ImageVideoWrapper;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.SVG;

import androidx.annotation.*;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.twisterrob.android.content.glide.*;
import net.twisterrob.inventory.android.data.R;
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
	public void test() throws IOException, IllegalAccessException, InterruptedException {
		LOG.error("SVG version {}", SVG.getVersion());
		final Context context = ApplicationProvider.getApplicationContext();
		File backup = context.getExternalFilesDir("svg");
		assumeTrue("External folder 'svg' is missing", backup != null);

		Field[] fields = R.raw.class.getFields();
		File dir = new File(context.getCacheDir(), "svg");
		IOTools.delete(dir);
		IOTools.ensure(dir);
		int cpus = Runtime.getRuntime().availableProcessors();
		LOG.info("Running on {} cores.", cpus);
		ExecutorService service = Executors.newFixedThreadPool(cpus);
		Set<String> exclusions = new HashSet<>(Arrays.asList(
				"shrink_resources",
				"icon_preview",
				"icon_helpers"
		));
		for (Field field : fields) {
			final String name = field.getName();
			if (exclusions.contains(name)) continue;
			final File target = new File(dir, name + ".png");
			@RawRes final int rawId = (Integer)field.get(null);
			service.submit(new Runnable() {
				@Override public void run() {
					try {
						saveSVG(context, rawId, target);
						LOG.info("Generated {}", target.getAbsolutePath());
					} catch (Exception ex) {
						try {
							String stackTrace = ObjectTools.getFullStackTrace(ex);
							IOTools.writeAll(new FileOutputStream(target), stackTrace);
							LOG.warn("{} failed: {}", name, ex, ex);
						} catch (IOException ex2) {
							// Shouldn't happen normally.
							throw new IllegalStateException(ex2);
						}
					}
				}
			});
		}
		service.shutdown();
		service.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);

		File zip = new File(dir.getParentFile(), dir.getName() + ".zip");
		IOTools.zip(zip, false, dir);
		LOG.info("ZIP: {}", zip.getAbsolutePath());
		File saved = new File(backup, "svg_" + VERSION.SDK_INT + ".zip");
		IOTools.copyFile(zip, saved);
		LOG.error("adb pull {}", saved.getAbsolutePath());
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
		// Simulate the following Glide load, but can't do this because of threading.
		//FutureTarget<Bitmap> target = Glide
		//		.with(context)
		//		.fromResource()
		//		.asBitmap()
		//		.diskCacheStrategy(DiskCacheStrategy.NONE)
		//		.decoder(getSvgDecoder(context))
		//		.load(svgRes)
		//		.into(1024, 1024)
		//		.get();
		ResourceDecoder<InputStream, Bitmap> decoder = getSvgImageDecoder(context);
		InputStream stream = context.getResources().openRawResource(svgRes);
		Resource<Bitmap> resource =
				decoder.decode(stream, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
		try {
			Bitmap bitmap = resource.get();
			save(bitmap, file);
		} finally {
			resource.recycle();
		}
	}

	private static @NonNull ResourceDecoder<ImageVideoWrapper, Bitmap> getSvgDecoder(
			@NonNull Context context) {
		final ResourceDecoder<InputStream, Bitmap> decoder = getSvgImageDecoder(context);

		// like ImageVideoBitmapDecoder, except it doesn't swallow the exception
		return new ResourceDecoder<ImageVideoWrapper, Bitmap>() {
			@Override public Resource<Bitmap> decode(
					ImageVideoWrapper source, int width, int height) throws IOException {
				return decoder.decode(source.getStream(), width, height);
			}
			@Override public String getId() {
				return "ImageVideoBitmapDecoder-WithExceptions";
			}
		};
	}
	private static @NonNull ResourceDecoder<InputStream, Bitmap> getSvgImageDecoder(
			@NonNull Context context) {
		BitmapPool pool = Glide.get(context).getBitmapPool();
		return new SvgBitmapDecoder(pool, new RawResourceSVGExternalFileResolver(context, pool));
	}
}
