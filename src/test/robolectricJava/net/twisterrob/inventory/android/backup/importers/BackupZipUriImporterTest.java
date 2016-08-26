package net.twisterrob.inventory.android.backup.importers;

import java.io.*;

import org.junit.*;
import org.mockito.*;
import org.slf4j.*;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.isA;

import android.content.*;
import android.net.Uri;

import net.twisterrob.android.test.LoggingAnswer;
import net.twisterrob.inventory.android.backup.ImportProgressHandler;
import net.twisterrob.test.frameworks.RobolectricTestBase;

import static net.twisterrob.test.HasCause.*;
import static net.twisterrob.test.SameFile.*;

public class BackupZipUriImporterTest extends RobolectricTestBase {
	private static final Logger LOG = LoggerFactory.getLogger(BackupZipUriImporterTest.class);

	public static final File FILE = new File("a/b");
	public static final Uri NON_FILE = Uri.parse("files:///non-existent-protocol");

	@Mock private Context context;
	@Mock private ImportProgressHandler dispatcher;
	@Mock private BackupZipStreamImporter streamImporter;
	@Mock private BackupZipFileImporter fileImporter;
	@InjectMocks private BackupZipUriImporter importer;

	@Before public void defaultStubbing() {
		when(context.getContentResolver()).thenReturn(mock(ContentResolver.class));
		doAnswer(new LoggingAnswer<>(LOG)).when(dispatcher).warning(anyString());
		doAnswer(new LoggingAnswer<>(LOG)).when(dispatcher).error(anyString());
	}

	@Test public void testImportFromFileCallsFileImporter() throws Exception {
		Uri input = Uri.fromFile(FILE);

		importer.importFrom(input);

		verify(fileImporter).importFrom(argThat(pointsTo(FILE)));
		verifyZeroInteractions(streamImporter);
	}

	@Test public void testImportFromNonFileCallsStreamImporter() throws Exception {
		when(context.getContentResolver().openInputStream(any(Uri.class))).thenReturn(mock(InputStream.class));

		importer.importFrom(NON_FILE);

		verify(streamImporter).importFrom(isA(InputStream.class));
		verifyZeroInteractions(fileImporter);
	}

	@Test public void testImportFromFileFails() throws Exception {
		NullPointerException ex = new NullPointerException("test");
		doThrow(ex).when(fileImporter).importFrom(any(File.class));

		thrown.expect(hasCause(ex));
		importer.importFrom(Uri.fromFile(FILE));
	}

	@Test public void testImportFromNonFileFails() throws Exception {
		when(context.getContentResolver().openInputStream(any(Uri.class))).thenReturn(mock(InputStream.class));
		NullPointerException ex = new NullPointerException("test");
		doThrow(ex).when(streamImporter).importFrom(any(InputStream.class));

		thrown.expect(hasCause(ex));
		importer.importFrom(NON_FILE);
	}

	@Test public void testImportFromNonFileFailsToOpen() throws Exception {
		FileNotFoundException ex = new FileNotFoundException("test");
		when(context.getContentResolver().openInputStream(NON_FILE)).thenThrow(new IllegalArgumentException(
				new IllegalStateException(new IOException(ex))));

		thrown.expect(hasCause(ex));
		importer.importFrom(NON_FILE);
	}
}
