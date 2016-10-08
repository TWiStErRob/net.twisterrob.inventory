package net.twisterrob.android.test.junit;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** Useful for detecting test failure in {@link org.junit.After} methods. */
public class TestWatcherStatus extends TestWatcher {
	boolean succeeded, failed, skipped, started, finished;
	private Description description;
	@Override protected void starting(Description description) {
		this.description = description;
		started = true;
	}
	@Override protected void succeeded(Description description) {
		succeeded = true;
	}
	@Override protected void skipped(AssumptionViolatedException e, Description description) {
		skipped = true;
	}
	@Override protected void failed(Throwable e, Description description) {
		failed = true;
	}
	@Override protected void finished(Description description) {
		finished = true;
	}

	public Description getDescription() {
		return description;
	}
	public boolean isSucceeded() {
		return succeeded;
	}
	public boolean isFailed() {
		return failed;
	}
	public boolean isSkipped() {
		return skipped;
	}
	public boolean isStarted() {
		return started;
	}
	public boolean isFinished() {
		return finished;
	}
}
