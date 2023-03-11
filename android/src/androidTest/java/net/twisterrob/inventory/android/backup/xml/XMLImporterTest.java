package net.twisterrob.inventory.android.backup.xml;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.xml.sax.SAXParseException;

import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.res.Resources;

import androidx.test.core.app.ApplicationProvider;

import net.twisterrob.inventory.android.backup.Importer.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.PropertyType;
import net.twisterrob.inventory.android.content.model.Types;
import net.twisterrob.java.io.IOTools;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class XMLImporterTest {

	@Rule public final MockitoRule mockito = MockitoJUnit.rule();

	@Mock Database mockDatabase;
	@Mock Types mockTypes;
	@Mock ImportProgress mockProgress;
	@Mock ImportImageGetter mockImages;

	private XMLImporter sut;

	@Before public void setUp() {
		Resources resources = ApplicationProvider.getApplicationContext().getResources();
		sut = new XMLImporter(resources, mockDatabase, mockTypes);
	}

	private void callSut(String xml) throws Exception {
		sut.doImport(IOTools.stream(xml), mockProgress, mockImages);
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

		SAXParseException ex = assertThrows(SAXParseException.class, new ThrowingRunnable() {
			@Override public void run() throws Exception {
				callSut(xml);
			}
		});

		assertThat(ex, hasMessage("At line 3, column 7: not well-formed (invalid token)"));
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
}
