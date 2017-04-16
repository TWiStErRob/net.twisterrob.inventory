package net.twisterrob.android.test.junit;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static org.junit.Assert.*;

/**
 * Useful for detecting test failure in {@link org.junit.After} methods.
 *
 * @deprecated this class is useless in practice, because succeeded/failed/skipped is called after the
 * {@link org.junit.After}s. Use an anonymous inner class {@link TestWatcher} {@link org.junit.Rule} to listen.
 */
public class TestWatcherStatus extends TestWatcher {

	private boolean succeeded, failed, skipped, started, finished;
	private Description description;

	@Override protected void starting(Description description) {
		this.description = description;
		started = true;
	}

	@Override protected void succeeded(Description description) {
		assertSame(this.description, description);
		succeeded = true;
	}

	@Override protected void skipped(AssumptionViolatedException e, Description description) {
		assertSame(this.description, description);
		skipped = true;
	}

	@Override protected void failed(Throwable e, Description description) {
		assertSame(this.description, description);
		failed = true;
	}

	@Override protected void finished(Description description) {
		assertSame(this.description, description);
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
