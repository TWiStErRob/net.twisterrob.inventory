package net.twisterrob.android.test.espresso;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.*;
import org.slf4j.*;

import android.annotation.*;
import android.app.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build.*;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.runner.lifecycle.Stage;
import android.support.test.uiautomator.UiDevice;
import android.view.View;

import net.twisterrob.android.test.junit.InstrumentationExtensions;
import net.twisterrob.android.utils.tools.IOTools;

public class ScreenshotFailure implements TestRule {
	private static final Logger LOG = LoggerFactory.getLogger(ScreenshotFailure.class);
	public static final String DEFAULT_FOLDER_NAME = "testruns";
	private final File targetDir;
	private final Instrumentation instrumentation;

	public ScreenshotFailure() {
		this(InstrumentationRegistry.getInstrumentation());
	}
	public ScreenshotFailure(@NonNull Instrumentation instrumentation) {
		this(instrumentation, getDefaultDir(instrumentation));
	}
	public ScreenshotFailure(@NonNull Instrumentation instrumentation, @NonNull File targetDir) {
		this.instrumentation = instrumentation;
		this.targetDir = targetDir;
	}

	private static @NonNull File getDefaultDir(@NonNull Instrumentation instrumentation) {
		Context context = instrumentation.getContext();

		File result = context.getExternalFilesDir(DEFAULT_FOLDER_NAME);
		if (result == null) {
			result = new File(context.getFilesDir(), DEFAULT_FOLDER_NAME);
		}
		return result;
	}

	@Override public Statement apply(final Statement base, final Description description) {
		return new ScreenshotStatement(base, description);
	}

	private class ScreenshotStatement extends Statement {
		private final Statement base;
		private final Description description;
		public ScreenshotStatement(Statement base, Description description) {
			this.base = base;
			this.description = description;
		}
		@TargetApi(VERSION_CODES.KITKAT)
		@Override public void evaluate() throws Throwable {
			long started = System.currentTimeMillis();
			try {
				base.evaluate();
			} catch (Throwable ex) {
				try {
					String dirName = String.format(Locale.ROOT, "%s", description.getClassName());
					String shotName = String.format(Locale.ROOT, "%d_%s.png", started, description.getMethodName());
					File shot = takeScreenshot(dirName, shotName);
					LOG.info("Screenshot taken to {}", getADBPullPath(shot));
				} catch (Throwable shotEx) {
					if (VERSION_CODES.KITKAT <= VERSION.SDK_INT) {
						ex.addSuppressed(shotEx);
					} else {
						throw new MultipleFailureException(Arrays.asList(ex, shotEx));
					}
				}
				throw ex;
			}
		}
	}

	@SuppressLint("SdCardPath")
	private static File getADBPullPath(File shot) {
		if (shot == null) {
			return null;
		}
		return new File(shot.getAbsolutePath().replace("/storage/emulated/0/", "/sdcard/"));
	}

	/**
	 * Ignored <code>com.googlecode.eyesfree.utils.ScreenshotUtils.createScreenshot(getTargetContext());</code>,
	 * because it requires {@code  android.permission.READ_FRAME_BUFFER} which is signature level.
	 */
	@TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
	private File takeScreenshot(@NonNull String dirName, @NonNull String shotName) throws IOException {
		File dir = new File(targetDir, dirName);
		File target = new File(dir, shotName);
		IOTools.ensure(dir);
		if (VERSION_CODES.JELLY_BEAN_MR1 <= VERSION.SDK_INT) {
			LOG.trace("Taking screenshot with UiDevice into {}", target);
			UiDevice.getInstance(instrumentation).takeScreenshot(target);
			return target;
		}
		Bitmap shot = null;
		if (VERSION_CODES.JELLY_BEAN_MR2 <= VERSION.SDK_INT) {
			LOG.trace("Taking screenshot with UiAutomation", target);
			shot = instrumentation.getUiAutomation().takeScreenshot();
		}
		if (shot == null) {
			LOG.trace("Taking screenshot with drawing cache.");
			try {
				Activity activity = InstrumentationExtensions.getActivityInStage(Stage.RESUMED);
				shot = shootActivity(activity);
			} catch (Exception ex) {
				LOG.warn("No activity found to shoot.", ex);
				return null;
			}
		}
		if (shot != null) {
			FileOutputStream stream = new FileOutputStream(target);
			try {
				shot.compress(CompressFormat.PNG, 100, stream);
			} finally {
				IOTools.ignorantClose(stream);
			}
			return target;
		}
		throw new IllegalStateException("Cannot take screenshot in any way.");
	}

	private Bitmap shootActivity(final Activity activity) {
		final AtomicReference<Bitmap> viewShot = new AtomicReference<>();
		instrumentation.runOnMainSync(new Runnable() {
			@Override public void run() {
				View view = activity.getWindow().getDecorView();
				LOG.trace("Taking screenshot of {}: {}", activity, HumanReadables.describe(view));
				if (!view.isDrawingCacheEnabled()) {
					view.setDrawingCacheEnabled(true);
					view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
				}
				viewShot.set(view.getDrawingCache());
			}
		});
		return viewShot.get();
	}
}

