package net.twisterrob.inventory.android.test;

import java.util.Locale;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import net.twisterrob.android.test.automators.UiAutomatorExtensions;

public class ExternalAppKiller extends ExternalResource {
	private static final @NonNull Logger LOG = LoggerFactory.getLogger(ExternalAppKiller.class);

	private final @NonNull String packageName;
	private final int maxBackAttempts;
	private final int maxHomeAttempts;

	public ExternalAppKiller(@NonNull String name) {
		this(name, 10, 3);
	}

	public ExternalAppKiller(@NonNull String name, int maxBackAttempts, int maxHomeAttempts) {
		packageName = name;
		this.maxBackAttempts = maxBackAttempts;
		this.maxHomeAttempts = maxHomeAttempts;
	}

	@Override protected void after() {
		int backAttempts = softKillAppWith(maxBackAttempts, packageName, new Runnable() {
			@Override public void run() {
				LOG.warn("{} is still open, trying to escape it with Back press...", packageName);
				UiAutomatorExtensions.pressBackExternalUnsafe();
			}
		});
		int homeAttempts = softKillAppWith(maxHomeAttempts, packageName, new Runnable() {
			@Override public void run() {
				LOG.warn("{} is still open, stuck on going Back, going Home now...", packageName);
				UiDevice.getInstance(getInstrumentation()).pressHome();
			}
		});
		if (backAttempts > 0 || homeAttempts > 0) {
			if (isVisible(packageName)) {
				fail(String.format(Locale.ROOT,
						"%s is still open even after %d Back presses and %d Home presses.",
						packageName, backAttempts, homeAttempts));
			} else {
				fail(String.format(Locale.ROOT,
						"%s was still open at the end of the test, "
								+ "pressing Back %d times and Home %d times helped kill it.",
						packageName, backAttempts, homeAttempts));
			}
		} else {
			// Normal behavior, the package was not visible from the get go.
		}
	}

	@CheckResult
	private static int softKillAppWith(int maxAttempts, @NonNull String packageName, Runnable killAction) {
		int attempts = 0;
		while (attempts < maxAttempts && isVisible(packageName)) {
			attempts++;
			killAction.run();
		}
		return attempts;
	}

	@CheckResult
	private static boolean isVisible(@NonNull String packageName) {
		try {
			UiDevice.getInstance(getInstrumentation());
		} catch(IllegalStateException e) {
			// UiDevice.getInstance() throws IllegalStateException if the instrumentation is not running.
			return false;
		}
		return packageName.equals(UiAutomatorExtensions.getCurrentAppPackageName());
	}
}
