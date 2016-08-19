package net.twisterrob.inventory.android.backup;

import java.io.*;
import java.util.function.Function;

import org.junit.*;
import org.junit.rules.*;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.*;
import org.mockito.stubbing.Answer;

import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import com.diffplug.common.base.Errors;
import com.shazam.gwen.Gwen;

import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.backup.Importer.ImportProgressHandler;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.Type;
import net.twisterrob.test.PackageNameShortener;

public abstract class BackupZipImporterTestBase {
	@Rule public MockitoRule mockito = MockitoJUnit.rule();
	@Rule public TemporaryFolder temp = new TemporaryFolder();
	@Rule public TestRule packageNameShortener = new PackageNameShortener();

	protected static final String NO_IMAGE = null;
	protected static final String[] NONE = new String[0];
	protected static final String IMAGE1 = "item_1_00000000_000000.jpg";
	protected static final String IMAGE2 = "item_2_00000000_000000.jpg";
	protected static final String IMAGE3 = "item_3_00000000_000000.jpg";
	protected static final String IMAGE4 = "item_4_00000000_000000.jpg";

	@Mock protected ProgressDispatcher dispatcherMock;
	@Mock protected XMLImporter xmlImporterMock;
	@Mock protected Database dbMock;

	@InjectMocks protected BackupZip input;
	@InjectMocks protected BackupImageDatabase database;
	protected BackupImporter importer;

	@Before public void initImporter() {
		Function<InputStream, Progress> callImport = Errors.rethrow().wrap(this::callImport);
		importer = new BackupImporter(dbMock, dispatcherMock, xmlImporterMock, callImport);
	}

	protected abstract Progress callImport(InputStream stream) throws IOException;

	@After public void noMoreInteractions() {
		verify(dispatcherMock, atLeastOnce()).dispatchProgress(any(Progress.class));
		verifyNoMoreInteractions(dispatcherMock, xmlImporterMock, dbMock);
	}

	@Test public void testEmptyZip() throws Throwable {
		Gwen.given(input);
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(database).transacted().incompletely();
		Gwen.then(result).started().failedWith("missing data").failedWith(Paths.BACKUP_DATA_FILENAME);
	}

	@Test public void testOnlyXMLData() throws Throwable {
		Gwen.given(input).withDataXML().withImages(NONE);
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).transacted().successfully();
		Gwen.then(result).started().successful();
	}

	@Test public void testWithoutImages() throws Throwable {
		Gwen.given(input)
		    .withImages(NONE)
		    .withDataXML()
		    .containing(1, "item 1", NO_IMAGE)
		    .containing(2, "item 2", NO_IMAGE)
		    .containing(3, "item 3", NO_IMAGE)
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).transacted().successfully();
		Gwen.then(result).started().successful().importedItems(3, 3).importedImages(0, 0).noInvalidImages();
	}

	@Test public void testWithImages() throws Throwable {
		Gwen.given(input)
		    .withImages(IMAGE1, IMAGE2)
		    .withDataXML()
		    .containing(1, "item 1", IMAGE1)
		    .containing(2, "item 2", NO_IMAGE)
		    .containing(3, "item 3", IMAGE2)
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).transacted().successfully().matchedImages(IMAGE1, IMAGE2);
		Gwen.then(result).started().successful().importedItems(3, 3).importedImages(2, 2).noInvalidImages();
	}

	@Test public void testWithMissingImageReferences() throws Throwable {
		Gwen.given(input)
		    .withImages(NONE)
		    .withDataXML()
		    .containing(1, "item 1", IMAGE1)
		    .containing(2, "item 2", NO_IMAGE)
		    .containing(3, "item 3", IMAGE3)
		    .containing(4, "item 4", NO_IMAGE)
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).transacted().successfully();
		Gwen.then(result).started().successful().importedItems(4, 4).importedImages(0, 2).invalidImages(IMAGE1, IMAGE3);
	}
	@Test public void testWithPartialMissingImageReferences() throws Throwable {
		Gwen.given(input)
		    .withDataXML()
		    .containing(1, "item 1", IMAGE1)
		    .containing(2, "item 2", NO_IMAGE)
		    .containing(3, "item 3", IMAGE3) // missing
		    .withImage(IMAGE1)
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).transacted().successfully().matchedImages(IMAGE1);
		Gwen.then(result).started().successful().importedItems(3, 3).importedImages(1, 2).invalidImages(IMAGE3);
	}

	@Test public void testWithExtraImagesLast() throws Throwable {
		Gwen.given(input)
		    .withDataXML()
		    .containing(1, "item 1", IMAGE1)
		    .containing(2, "item 2", IMAGE2)
		    .withImages(IMAGE1, IMAGE2, IMAGE3, IMAGE4) // 3 and 4 are extras
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).transacted().successfully().matchedImages(IMAGE1, IMAGE2);
		Gwen.then(result).started().successful().importedItems(2, 2).importedImages(2, 2).noInvalidImages();
	}

	@Test public void testInvalidImageType() throws Throwable {
		Gwen.given(input).withDataXML().withImages(IMAGE1);
		BackupImportResult result = Gwen.when(importer).imports(input, new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				ImportProgressHandler handler = invocation.getArgumentAt(1, ImportProgressHandler.class);
				handler.importImage(Type.Root, 0, "item name", IMAGE1);
				return null;
			}
		});
		Gwen.then(importer).importedXML();
		Gwen.then(database).transacted().incompletely();
		Gwen.then(result).started().failedWith(Type.Root.toString());
		verify(dbMock, atMost(1)).addImage(any(byte[].class), anyLong());
	}

	@Test public void testCannotCommit() throws Throwable {
		Gwen.given(input).withDataXML();
		doThrow(new IllegalStateException("Test cannot commit")).when(dbMock).endTransaction();
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).transacted().successfully();
		Gwen.then(result).started().failedWith("Test cannot commit");
	}

	@Test public void testCannotCommitAfterError() throws Throwable {
		Gwen.given(input).withDataXML();
		doThrow(new IllegalStateException("Test cannot commit")).when(dbMock).endTransaction();
		BackupImportResult result = Gwen.when(importer).imports(input, new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				throw new IllegalStateException("Test failing import");
			}
		});
		Gwen.then(importer).importedXML();
		Gwen.then(database).transacted().incompletely();
		Gwen.then(result).started().failedWith("Test failing import").hasWarning(containsString("Test cannot commit"));
	}
}
