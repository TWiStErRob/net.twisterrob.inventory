package net.twisterrob.inventory.android.backup.xml;

import java.io.*;
import java.util.zip.ZipFile;

import static java.util.Collections.*;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.junit.rules.TemporaryFolder;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.res.AssetManager;
import android.database.*;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.backup.Exporter;
import net.twisterrob.inventory.android.content.Database;

import static net.twisterrob.test.hamcrest.Matchers.hasEntry;
import static net.twisterrob.test.hamcrest.Matchers.*;

public class ZippedXMLExporterTest {

	private static final byte[] BOM = {(byte)0xEF, (byte)0xBB, (byte)0xBF};

	@Rule public final TemporaryFolder temp = new TemporaryFolder();

	@Mock Database mockDatabase;
	@Mock CursorExporter mockExporter;

	private Exporter sut;

	@Before public void setUp() {
		MockitoAnnotations.initMocks(this);
		AssetManager assetManager = ApplicationProvider.getApplicationContext().getAssets();

		sut = new ZippedXMLExporter(mockDatabase, assetManager, mockExporter);
	}

	private void callSut(Cursor cursor, OutputStream zip) throws Throwable {
		sut.initExport(zip);

		sut.initData(cursor);
		cursor.moveToPosition(-1);
		while (cursor.moveToNext()) {
			sut.writeData(cursor);
		}
		sut.finishData(cursor);

		sut.initImages(cursor);
		sut.finishImages(cursor);

		sut.finishExport();
		sut.finalizeExport();
	}

	@Test public void testEmptyOutput() throws Throwable {
		final String xml = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n"
				+ "<inventory approximateCount=\"0\" version=\"1.0\">\n"
				+ "</inventory>\n";
		Cursor cursor = new MatrixCursor(new String[0]);
		stubXML(xml, cursor);

		ByteArrayOutputStream zipOs = new ByteArrayOutputStream();
		callSut(cursor, zipOs);

		verify(mockExporter).start(ArgumentMatchers.<OutputStream>any(), eq(cursor));
		verify(mockExporter).finish(cursor);
		verifyNoMoreInteractions(mockDatabase, mockExporter);

		try (ZipFile zip = saveTempZip(zipOs)) {
			assertThat(list(zip.entries()), hasSize(5));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "data.xml"));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "data.xml.xsd"));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "data.xml.xslt"));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "inventory.html"));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "inventory.csv"));
			assertThat(zip, hasEntry(
					allOf(zipEntryWithName("data.xml"), zipEntryWithContent(zip, xml))
			));
		}
	}

	@Test public void testInvalidOutput() throws Throwable {
		final Cursor cursor = new MatrixCursor(new String[0]);
		stubXML("", cursor);

		final ByteArrayOutputStream zipOs = new ByteArrayOutputStream();
		IOException ex = assertThrows(IOException.class, new ThrowingRunnable() {
			@Override public void run() throws Throwable {
				callSut(cursor, zipOs);
			}
		});

		assertThat(ex, hasMessage(startsWith("Cannot re-parse exported XML")));
		verify(mockExporter).start(ArgumentMatchers.<OutputStream>any(), eq(cursor));
		verify(mockExporter).finish(cursor);
		verifyNoMoreInteractions(mockDatabase, mockExporter);
	}

	@Test public void testDemoXmlTransformation() throws Throwable {
		AssetManager assets = ApplicationProvider.getApplicationContext().getAssets();
		String xml = IOTools.readAll(assets.open("demo.xml"));
		Cursor cursor = new MatrixCursor(new String[0]);
		stubXML(xml, cursor);

		ByteArrayOutputStream zipOs = new ByteArrayOutputStream();
		callSut(cursor, zipOs);

		verify(mockExporter).start(ArgumentMatchers.<OutputStream>any(), eq(cursor));
		verify(mockExporter).finish(cursor);
		verifyNoMoreInteractions(mockDatabase, mockExporter);

		try (ZipFile zip = saveTempZip(zipOs)) {
			assertThat(list(zip.entries()), hasSize(5));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "data.xml"));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "data.xml.xsd"));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "data.xml.xslt"));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "inventory.html"));
			assertThat(zip, hasPracticallyNonEmptyEntry(zip, "inventory.csv"));
			assertThat(zip, hasEntry(
					allOf(zipEntryWithName("data.xml"), zipEntryWithContent(zip, xml))
			));
		}
	}

	private @NonNull ZipFile saveTempZip(ByteArrayOutputStream zipOs) throws IOException {
		File zipFile = temp.newFile();
		IOTools.writeAll(new FileOutputStream(zipFile), zipOs.toByteArray());
		return new ZipFile(zipFile);
	}

	private void stubXML(final String xml, Cursor cursor) throws IOException {
		doAnswer(new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				OutputStream os = invocation.getArgument(0);
				IOTools.copyStream(IOTools.stream(xml), os, false);
				return null;
			}
		}).when(mockExporter).start(ArgumentMatchers.<OutputStream>any(), eq(cursor));
	}

	private static @NonNull Matcher<ZipFile> hasPracticallyNonEmptyEntry(ZipFile zip, String name) {
		return hasEntry(allOf(hasNonEmptyEntry(name), zipEntryWithContent(zip, not(equalTo(BOM)))));
	}
}
