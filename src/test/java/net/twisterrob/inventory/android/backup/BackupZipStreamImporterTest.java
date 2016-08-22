package net.twisterrob.inventory.android.backup;

import java.io.InputStream;

import org.junit.*;
import org.mockito.*;

import static org.mockito.Mockito.*;

import android.content.res.Resources;

import com.shazam.gwen.Gwen;

import net.twisterrob.android.test.GetStringVarargsAnswer;
import net.twisterrob.inventory.android.R;

// TODO make sure total image count doesn't include redundant images
// TODO add assertions for counts
public class BackupZipStreamImporterTest extends BackupZipImporterTestBase {
	@Mock private Resources res;
	@InjectMocks BackupZipStreamImporter realImporter;

	@Before public void setUp() {
		when(res.getString(anyInt(), Matchers.anyVararg())).thenAnswer(new GetStringVarargsAnswer(R.string.class));
	}

	@Override protected void callImport(InputStream stream) throws Exception {
		realImporter.importFrom(stream);
	}

	@Test public void testWithExtraImagesFirst() throws Throwable {
		Gwen.given(input)
		    .withImages(IMAGE1, IMAGE2, IMAGE3, IMAGE4) // 3 and 4 are extras
		    .withDataXML()
		    .containing(1, "item 1", IMAGE1)
		    .containing(2, "item 2", IMAGE2)
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).matchedImages(IMAGE1, IMAGE2).redundantImages(IMAGE3, IMAGE4);
		Gwen.then(result).started().successful().importedItems(2, 2).importedImages(2, 4).noInvalidImages();
	}

	@Test public void testXMLThenImagesNoItems() throws Throwable {
		Gwen.given(input).withDataXML().withImages(IMAGE1, IMAGE2, IMAGE3);
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).danglingImages(IMAGE1, IMAGE2, IMAGE3);
		Gwen.then(result).started().successful().importedItems(0, 0).importedImages(0, 0).noInvalidImages();
	}

	@Test public void testImagesThenXMLNoItems() throws Throwable {
		Gwen.given(input).withImages(IMAGE1, IMAGE2, IMAGE3).withDataXML();
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).redundantImages(IMAGE1, IMAGE2, IMAGE3);
		Gwen.then(result).started().successful().importedItems(0, 0).importedImages(0, 3);
	}

	@Test public void testXMLBetweenImagesNoItems() throws Throwable {
		Gwen.given(input).withImages(IMAGE1, IMAGE2).withDataXML().withImages(IMAGE3, IMAGE4);
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).redundantImages(IMAGE1, IMAGE2);
		Gwen.then(result).started().successful().noInvalidImages();
	}

	@Test public void testXMLBetweenImagesBeforeOnly() throws Throwable {
		Gwen.given(input)
		    .withImages(IMAGE1, IMAGE2)
		    .withDataXML()
		    .containing(1, "item 1 before", IMAGE1)
		    .containing(2, "item 2 before", IMAGE2)
		    .withImages(IMAGE3, IMAGE4)
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).matchedImages(IMAGE1, IMAGE2).danglingImages(IMAGE3, IMAGE4);
		Gwen.then(result).started().successful().noInvalidImages();
	}

	@Test public void testXMLBetweenImagesAfterOnly() throws Throwable {
		Gwen.given(input)
		    .withImages(IMAGE1, IMAGE2)
		    .withDataXML()
		    .containing(3, "item 3 after", IMAGE3)
		    .containing(4, "item 4 after", IMAGE4)
		    .withImages(IMAGE3, IMAGE4)
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).matchedImages(IMAGE3, IMAGE4).redundantImages(IMAGE1, IMAGE2);
		Gwen.then(result).started().successful().noInvalidImages();
	}

	@Test public void testXMLBetweenImagesPartial() throws Throwable {
		Gwen.given(input)
		    .withImages(IMAGE1, IMAGE2)
		    .withDataXML()
		    .containing(2, "item 2 before", IMAGE2)
		    .containing(3, "item 3 after", IMAGE3)
		    .withImages(IMAGE3, IMAGE4)
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).redundantImages(IMAGE1).matchedImages(IMAGE2, IMAGE3).danglingImages(IMAGE4);
		Gwen.then(result).started().successful().noInvalidImages();
	}

	@Test public void testXMLBetweenImagesAll() throws Throwable {
		Gwen.given(input)
		    .withImages(IMAGE1, IMAGE2)
		    .withDataXML()
		    .containing(1, "item 1 before", IMAGE1)
		    .containing(2, "item 2 before", IMAGE2)
		    .containing(3, "item 3 after", IMAGE3)
		    .containing(4, "item 4 after", IMAGE4)
		    .withImages(IMAGE3, IMAGE4)
		;
		BackupImportResult result = Gwen.when(importer).imports(input);
		Gwen.then(importer).importedXML();
		Gwen.then(database).matchedImages(IMAGE1, IMAGE2, IMAGE3, IMAGE4);
		Gwen.then(result).started().successful().noInvalidImages();
	}
}
