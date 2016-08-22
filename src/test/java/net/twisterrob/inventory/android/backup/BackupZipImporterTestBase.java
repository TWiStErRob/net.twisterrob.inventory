package net.twisterrob.inventory.android.backup;

import java.io.InputStream;
import java.util.function.Consumer;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.rules.*;
import org.mockito.*;
import org.mockito.junit.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.internal.matchers.ThrowableMessageMatcher.*;
import static org.mockito.Mockito.*;

import com.diffplug.common.base.Errors;
import com.shazam.gwen.Gwen;

import static com.github.stefanbirkner.fishbowl.Fishbowl.*;

import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.Database;
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

	@Spy protected ImportProgressHandler dispatcherMock;
	@Mock protected XMLImporter xmlImporterMock;
	@Mock protected Database dbMock;

	@InjectMocks protected BackupZip input;
	@InjectMocks protected BackupImageDatabase database; // TODO change to BackupDatabase
	protected BackupImporter importer;

	@Before public void initImporter() {
		Consumer<InputStream> callImport = Errors.rethrow().wrap(this::callImport);
		importer = new BackupImporter(dispatcherMock, xmlImporterMock, callImport);
	}

	protected abstract void callImport(InputStream stream) throws Exception;

	@After public void noMoreInteractions() {
		verify(dispatcherMock, atLeastOnce()).publishProgress();
		verifyNoMoreInteractions(xmlImporterMock, dbMock); // TODO add dispatcherMock
	}

	@Test public void testEmptyZip() throws Throwable {
		Gwen.given(input);

		Throwable thrown = exceptionThrownBy(() -> Gwen.when(importer).imports(input));

		Matcher<String> aboutMissingXML =
				both(containsStringIgnoringCase("missing data")).and(containsString(Paths.BACKUP_DATA_FILENAME));
		assertThat(thrown, hasMessage(aboutMissingXML));
	}

	@Test public void testOnlyXMLData() throws Throwable {
		Gwen.given(input).withDataXML().withImages(NONE);
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
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
		Gwen.then(database).matchedImages(IMAGE1, IMAGE2);
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
		Gwen.then(database).matchedImages(IMAGE1);
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
		Gwen.then(database).matchedImages(IMAGE1, IMAGE2);
		Gwen.then(result).started().successful().importedItems(2, 2).importedImages(2, 2).noInvalidImages();
	}
}
