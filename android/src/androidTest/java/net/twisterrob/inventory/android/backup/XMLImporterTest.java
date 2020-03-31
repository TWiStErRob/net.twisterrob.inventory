package net.twisterrob.inventory.android.backup;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.mockito.*;
import org.xml.sax.SAXParseException;

import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import net.twisterrob.inventory.android.backup.Importer.*;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.PropertyType;
import net.twisterrob.inventory.android.content.model.Types;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class XMLImporterTest {

	@Mock Database mockDatabase;
	@Mock Types mockTypes;
	@Mock ImportProgress mockProgress;
	@Mock ImportImageGetter mockImages;

	private XMLImporter sut;

	@Before public void setUp() {
		MockitoAnnotations.initMocks(this);
		Resources resources = InstrumentationRegistry.getTargetContext().getResources();
		sut = new XMLImporter(resources, mockDatabase, mockTypes);
	}

	private void callSut(String xml) throws Exception {
		sut.doImport(stream(xml), mockProgress, mockImages);
	}

	@Test public void testEmptyXMLDoesNotWriteDB() throws Exception {
		String xml = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
				+ "<inventory approximateCount=\"0\" version=\"1.0\">\n"
				+ "</inventory>\n";

		callSut(xml);

		verifyNoInteractions(mockDatabase, mockTypes, mockImages);
	}

	@Test public void testEmptyXMLWithCountPublishes() throws Exception {
		String xml = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
				+ "<inventory approximateCount=\"42\" version=\"1.0\">\n"
				+ "</inventory>\n";

		callSut(xml);

		verify(mockProgress).publishStart(42);
		verifyNoMoreInteractions(mockDatabase, mockTypes, mockProgress, mockImages);
	}

	@Test public void testInvalidXML() {
		final String xml = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
				+ "<inventory approximateCount=\"0\" version=\"1.0\">\n"
				+ "<!-- -- -->\n"
				+ "</inventory>\n";

		SAXParseException expectedFailure =
				assertThrows(SAXParseException.class, new ThrowingRunnable() {
					@Override public void run() throws Exception {
						callSut(xml);
					}
				});

		assertThat(expectedFailure,
				hasMessage("At line 3, column 7: not well-formed (invalid token)"));
		verifyNoInteractions(mockDatabase, mockTypes, mockImages);
	}

	@Test public void testSinglePropertyAddedToDB() throws Exception {
		when(mockDatabase.findProperty(anyString())).thenReturn(null);
		when(mockTypes.getID(ArgumentMatchers.<String>any())).thenReturn(null);

		String xml = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
				+ "<inventory approximateCount=\"0\" version=\"1.0\">\n"
				+ "\t<property name=\"test\" />\n"
				+ "</inventory>\n";

		callSut(xml);

		verify(mockDatabase).findProperty("test");
		verify(mockTypes).getID(null);
		verify(mockDatabase).createProperty(PropertyType.DEFAULT, "test", null);
		verifyNoMoreInteractions(mockDatabase, mockTypes, mockImages);
	}

	private @NonNull InputStream stream(@NonNull String xml) {
		return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
	}
}
