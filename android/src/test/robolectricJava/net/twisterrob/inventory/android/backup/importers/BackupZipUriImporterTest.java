package net.twisterrob.inventory.android.backup.importers;

import java.io.*;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.mockito.*;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.*;
import android.net.Uri;

import net.twisterrob.inventory.android.TestIgnoreApp;
import net.twisterrob.test.frameworks.RobolectricTestBase;

import static net.twisterrob.test.hamcrest.Matchers.*;

@Config(application = TestIgnoreApp.class)
public class BackupZipUriImporterTest extends RobolectricTestBase {
	public static final Uri URI = Uri.parse("files:///non-existent-protocol");

	@Mock ContentResolver contentResolver;
	@Mock BackupZipStreamImporter streamImporter;
	@InjectMocks private BackupZipUriImporter importer;

	@Test public void testImportSuccess() throws Exception {
		InputStream mockStream = mock(InputStream.class);
		when(contentResolver.openInputStream(any(Uri.class))).thenReturn(mockStream);

		importer.importFrom(URI);

		verify(streamImporter).importFrom(mockStream);
		verifyNoMoreInteractions(streamImporter);
		verify(mockStream).close();
		verifyNoMoreInteractions(mockStream);
	}

	@Test public void testImportFails() throws Exception {
		InputStream mockStream = mock(InputStream.class);
		when(contentResolver.openInputStream(any(Uri.class))).thenReturn(mockStream);
		NullPointerException ex = new NullPointerException("test");
		doThrow(ex).when(streamImporter).importFrom(mockStream);

		Throwable expectedFailure = assertThrows(Throwable.class, new ThrowingRunnable() {
			@Override public void run() throws Exception {
				importer.importFrom(URI);
			}
		});
		assertThat(expectedFailure, containsCause(ex));
		verify(mockStream).close();
		verifyNoMoreInteractions(mockStream);
	}

	@Test public void testImportFailsToOpen() throws Exception {
		FileNotFoundException ex = new FileNotFoundException("test");
		when(contentResolver.openInputStream(URI)).thenThrow(new IllegalArgumentException(
				new IllegalStateException(new IOException(ex))));

		Throwable expectedFailure = assertThrows(Throwable.class, new ThrowingRunnable() {
			@Override public void run() throws Exception {
				importer.importFrom(URI);
			}
		});
		assertThat(expectedFailure, containsCause(ex));
	}
}
