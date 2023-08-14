package net.twisterrob.inventory.android;

import android.os.Bundle;

import androidx.annotation.NonNull;

import net.twisterrob.android.test.junit.AndroidJUnitRunner;

public class InventoryJUnitRunner extends AndroidJUnitRunner {
	/**
	 * @see androidx.test.internal.runner.RunnerArgs#ARGUMENT_TIMEOUT
	 * @noinspection JavadocReference
	 */
	private static final String ARGUMENT_TIMEOUT = "timeout_msec";

	@Override public void onCreate(Bundle arguments) {
		addGlobalDefaultTimeout(arguments);
		super.onCreate(arguments);
	}

	/**
	 * Applying the timeout here, instead of using {@link org.junit.rules.Timeout},
	 * because it doesn't handle {@code @Test(timeout=)} overrides.
	 * <p>
	 * {@link androidx.test.runner.AndroidJUnitRunner} lists {@value ARGUMENT_TIMEOUT} as deprecated,
	 * but the recommended replacement is weaker.
	 */
	private static void addGlobalDefaultTimeout(@NonNull Bundle arguments) {
		if (!arguments.containsKey(ARGUMENT_TIMEOUT)) {
			arguments.putLong(ARGUMENT_TIMEOUT, 20_000);
		}
	}
}
