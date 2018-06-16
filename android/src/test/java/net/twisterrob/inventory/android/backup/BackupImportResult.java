package net.twisterrob.inventory.android.backup;

import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.base.Preconditions;
import com.shazam.gwen.collaborators.Asserter;

@SuppressWarnings("UnusedReturnValue")
public class BackupImportResult implements Asserter {
	private final Progress progress;
	private final ImportProgressHandler dispatcher;
	public BackupImportResult(Progress progress, ImportProgressHandler dispatcher) {
		assertNotNull(progress);
		this.progress = progress;
		this.dispatcher = Preconditions.checkNotNull(dispatcher);
	}

	public BackupImportResult published(int count) {
		// atMost is a dirty hack to make Base test work with both File and Stream importer
		verify(dispatcher, atMost(count)).publishProgress();
		return this;
	}
	public BackupImportResult successful() {
		assertThat(progress.failure, nullValue());
		return this;
	}
	public BackupImportResult importedImages(int done, int total) {
		assertEquals("Total image count", total, progress.imagesTotal);
		assertEquals("Finished image count", done, progress.imagesDone);
		verify(dispatcher, atMost(done)).imageIncrement();
		verify(dispatcher, atMost(total)).imageTotalIncrement();
		return this;
	}
	public BackupImportResult importedItems(int done, int total) {
		assertEquals("Total item count", total, progress.total);
		assertEquals("Finished item count", done, progress.done);
		return this;
	}
	public BackupImportResult noInvalidImages() {
		assertThat(progress.warnings, not(hasItem(allOf(containsString("invalid"), containsString("image")))));
		return this;
	}
	public BackupImportResult invalidImages(String... images) {
		for (String image : images) {
			hasWarning(allOf(containsString("invalid"), containsString(image)));
		}
		verify(dispatcher, times(images.length)).warning(anyString());
		return this;
	}
	public BackupImportResult hasWarning(Matcher<String> message) {
		assertThat(progress.warnings, hasItem(message));
		return this;
	}
}
