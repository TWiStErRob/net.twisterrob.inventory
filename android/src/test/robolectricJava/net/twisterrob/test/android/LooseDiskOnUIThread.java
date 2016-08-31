package net.twisterrob.test.android;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.os.StrictMode;

public class LooseDiskOnUIThread implements TestRule {
	private final boolean allowWrites;
	public LooseDiskOnUIThread() {
		this(false);
	}
	public LooseDiskOnUIThread(boolean allowWrites) {
		this.allowWrites = allowWrites;
	}
	@Override public Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override public void evaluate() throws Throwable {
				StrictMode.ThreadPolicy originalPolicy = allowWrites
						? StrictMode.allowThreadDiskWrites()
						: StrictMode.allowThreadDiskReads();
				try {
					base.evaluate();
				} finally {
					StrictMode.setThreadPolicy(originalPolicy);
				}
			}
		};
	}
}
