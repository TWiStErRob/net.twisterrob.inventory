package net.twisterrob.inventory.android.backup;

import java.io.*;

import org.junit.*;
import org.mockito.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.*;

import android.content.*;
import android.net.Uri;

import net.twisterrob.test.frameworks.RobolectricTestBase;

import static net.twisterrob.test.HasCause.*;
import static net.twisterrob.test.SameFile.*;

public class BackupFileImporterTest extends RobolectricTestBase {
	public static final File FILE = new File("a/b");
	public static final Uri NON_FILE = Uri.parse("files:///non-existent-protocol");

	@Mock private Context context;
	@Mock private ProgressDispatcher dispatcher;
	@Mock private BackupZipStreamImporter streamImporter;
	@Mock private BackupZipFileImporter fileImporter;
	@InjectMocks private BackupZipUriImporter importer;
	private final Progress progress = new Progress();

	@Before public void defaultStubbing() {
		when(context.getContentResolver()).thenReturn(mock(ContentResolver.class));
	}

	@Test public void testImportFromFileCallsFileImporter() throws IOException {
		importer.importFrom(Uri.fromFile(FILE));

		verify(fileImporter).importFrom(argThat(pointsTo(FILE)));
	}

	@Test public void testImportFromNonFileCallsStreamImporter() throws IOException {
		when(context.getContentResolver().openInputStream(any(Uri.class))).thenReturn(mock(InputStream.class));

		importer.importFrom(NON_FILE);

		verify(streamImporter).importFrom(isA(InputStream.class));
	}

	@Test public void testImportFromFileReturns() throws IOException {
		when(fileImporter.importFrom(any(File.class))).thenReturn(progress);

		Progress progress = importer.importFrom(Uri.fromFile(FILE));

		assertSame(this.progress, progress);
	}

	@Test public void testImportFromNonFileReturns() throws IOException {
		when(context.getContentResolver().openInputStream(any(Uri.class))).thenReturn(mock(InputStream.class));
		when(streamImporter.importFrom(any(InputStream.class))).thenReturn(progress);

		Progress progress = importer.importFrom(NON_FILE);

		assertSame(this.progress, progress);
	}

	@Test public void testImportFromFileFails() throws IOException {
		NullPointerException ex = new NullPointerException("test");
		when(fileImporter.importFrom(any(File.class))).thenThrow(ex);

		Progress progress = importer.importFrom(Uri.fromFile(FILE));

		assertNotNull(progress);
		assertThat(progress.failure, hasCause(ex));
	}

	@Test public void testImportFromNonFileFails() throws IOException {
		when(context.getContentResolver().openInputStream(any(Uri.class))).thenReturn(mock(InputStream.class));
		NullPointerException ex = new NullPointerException("test");
		when(streamImporter.importFrom(any(InputStream.class))).thenThrow(ex);

		Progress progress = importer.importFrom(NON_FILE);

		assertNotNull(progress);
		assertThat(progress.failure, hasCause(ex));
	}

	@Test public void testImportFromNonFileFailsToOpen() throws IOException {
		FileNotFoundException ex = new FileNotFoundException("test");
		when(context.getContentResolver().openInputStream(NON_FILE)).thenThrow(new IllegalArgumentException(
				new IllegalStateException(new IOException(ex))));

		Progress progress = importer.importFrom(NON_FILE);

		assertNotNull(progress);
		assertThat(progress.failure, hasCause(ex));
	}
}
