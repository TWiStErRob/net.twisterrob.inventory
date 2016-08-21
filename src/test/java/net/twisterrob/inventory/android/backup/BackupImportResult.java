package net.twisterrob.inventory.android.backup;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.internal.matchers.ThrowableMessageMatcher.*;

import com.google.common.base.Preconditions;
import com.shazam.gwen.collaborators.Asserter;

class BackupImportResult implements Asserter {
	private final Progress progress;
	private final ImportProgressHandler dispatcher;
	public BackupImportResult(Progress progress, ImportProgressHandler dispatcher) {
		assertNotNull(progress);
		this.progress = progress;
		this.dispatcher = Preconditions.checkNotNull(dispatcher);
	}

	public BackupImportResult started() throws Throwable {
		// TODO verify progress
//		verify(dispatcher).dispatchProgress(notNull(Progress.class)); // publishStart
		return this;
	}
	public BackupImportResult successful() throws Throwable {
		assertThat(progress.failure, nullValue());
		return this;
	}
	public BackupImportResult failedWith(String message) {
		failedWith(hasMessage(containsString(message)));
		return this;
	}
	public BackupImportResult failedWith(Class<? extends Throwable> exception) {
		failedWith(org.hamcrest.Matchers.<Throwable>instanceOf(exception));
		return this;
	}
	public BackupImportResult failedWith(Matcher<Throwable> matcher) {
		assertThat(progress.failure, both(notNullValue(Throwable.class)).and(matcher));
		return this;
	}
	public BackupImportResult importedImages(int done, int total) {
		assertEquals("Total image count", total, progress.imagesTotal);
		assertEquals("Finished image count", done, progress.imagesDone);
		return this;
	}
	public BackupImportResult importedItems(int done, int total) {
		assertEquals("Total item count", total, progress.total);
		assertEquals("Finished item count", done, progress.done);
		return this;
	}
	public BackupImportResult noInvalidImages() {
		assertThat(progress.warnings, not(hasItem(both(containsString("invalid")).and(containsString("image")))));
		return this;
	}
	public BackupImportResult invalidImages(String... images) {
		for (String image : images) {
			assertThat(progress.warnings, hasItem(both(containsString("invalid")).and(containsString(image))));
		}
		return this;
	}
	public BackupImportResult hasWarning(Matcher<String> message) {
		assertThat(progress.warnings, hasItem(message));
		return this;
	}
}
