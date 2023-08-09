package net.twisterrob.inventory.android.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
import net.twisterrob.inventory.android.test.categories.OpensExternalApp;

public class ExternalAppKiller extends ExternalResource {
	private static final @NonNull Logger LOG = LoggerFactory.getLogger(ExternalAppKiller.class);

	private final @NonNull List<String> packageNames = new LinkedList<>();
	private final int maxBackAttempts;
	private final int maxHomeAttempts;

	public ExternalAppKiller(@NonNull String... packageNames) {
		this(10, 3, packageNames);
	}

	public ExternalAppKiller(int maxBackAttempts, int maxHomeAttempts) {
		this(maxBackAttempts, maxHomeAttempts, new String[0]);
	}

	public ExternalAppKiller(int maxBackAttempts, int maxHomeAttempts, @NonNull String... packageNames) {
		this.packageNames.addAll(Arrays.asList(packageNames));
		this.maxBackAttempts = maxBackAttempts;
		this.maxHomeAttempts = maxHomeAttempts;
	}

	@Override protected void after() {
		int backAttempts = softKillAppWith(maxBackAttempts, packageNames, new Runnable() {
			@Override public void run() {
				LOG.warn("{} is still open, trying to escape it with Back press...", packageNames);
				UiAutomatorExtensions.pressBackExternalUnsafe();
			}
		});
		int homeAttempts = softKillAppWith(maxHomeAttempts, packageNames, new Runnable() {
			@Override public void run() {
				LOG.warn("{} is still open, stuck on going Back, going Home now...", packageNames);
				UiDevice.getInstance(getInstrumentation()).pressHome();
			}
		});
		if (backAttempts > 0 || homeAttempts > 0) {
			if (isVisible(packageNames)) {
				fail(String.format(Locale.ROOT,
						"%s is still open even after %d Back presses and %d Home presses.",
						packageNames, backAttempts, homeAttempts));
			} else {
				fail(String.format(Locale.ROOT,
						"%s was still open at the end of the test, "
								+ "pressing Back %d times and Home %d times helped kill it.",
						packageNames, backAttempts, homeAttempts));
			}
		} else {
			// Normal behavior, the package was not visible from the get go.
		}
	}

	@CheckResult
	private static int softKillAppWith(int maxAttempts, @NonNull Collection<String> packageNames, Runnable killAction) {
		int attempts = 0;
		while (attempts < maxAttempts && isVisible(packageNames)) {
			attempts++;
			killAction.run();
		}
		return attempts;
	}

	@CheckResult
	private static boolean isVisible(@NonNull Collection<String> packageNames) {
		try {
			UiDevice.getInstance(getInstrumentation());
		} catch(IllegalStateException e) {
			// UiDevice.getInstance() throws IllegalStateException if the instrumentation is not running.
			return false;
		}
		return packageNames.contains(UiAutomatorExtensions.getCurrentAppPackageName());
	}
}
