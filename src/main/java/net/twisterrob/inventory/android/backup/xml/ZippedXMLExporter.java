package net.twisterrob.inventory.android.backup.xml;

import java.io.*;
import java.util.zip.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.slf4j.*;

import android.content.res.AssetManager;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.backup.exporters.ZippedExporter;
import net.twisterrob.java.io.TeeOutputStream;

public class ZippedXMLExporter extends ZippedExporter {
	private static final Logger LOG = LoggerFactory.getLogger(ZippedXMLExporter.class);

	public static final String XML_NAME = Paths.BACKUP_DATA_FILENAME;
	public static final String XSLT_NAME = Paths.BACKUP_DATA_FILENAME + ".xslt";
	public static final String CSV_NAME = "inventory.csv";
	public static final String HTML_NAME = "inventory.html";
	private final ByteArrayOutputStream capturedXML;

	public ZippedXMLExporter() {
		super(XML_NAME, new XMLExporter(XSLT_NAME));
		capturedXML = new ByteArrayOutputStream();
	}

	@Override protected OutputStream startStream(ZipOutputStream zip) throws IOException {
		// Google Drive upload opens two instances of the backup, we need to write something to the stream quick,
		// because otherwise it would take a long time for Google Drive to break one of them with EPIPE 
		copyXSLT(XSLT_NAME, App.getAppContext().getAssets().open("data.html.xslt"));
		zip.flush();
		OutputStream out = super.startStream(zip);
		return new TeeOutputStream(out, capturedXML);
	}

	@Override protected void endStream() throws IOException {
		byte[] xml = capturedXML.toByteArray();
		super.endStream();
		AssetManager assets = App.getAppContext().getAssets();
		transform(HTML_NAME, new ByteArrayInputStream(xml), assets.open("data.html.xslt"));
		transform(CSV_NAME, new ByteArrayInputStream(xml), assets.open("data.csv.xslt"));
	}

	private void copyXSLT(String zipFileName, InputStream stream) throws IOException {
		LOG.trace("Copying into {}", zipFileName);
		try {
			zip.putNextEntry(new ZipEntry(zipFileName));
			IOTools.copyStream(stream, zip, false);
			zip.closeEntry();
		} finally {
			IOTools.ignorantClose(stream);
			LOG.trace("Copying done: {}", zipFileName);
		}
	}

	private void transform(String zipFileName, InputStream xml, InputStream xslt) throws IOException {
		try {
			LOG.trace("Transforming into {}", zipFileName);
			zip.putNextEntry(new ZipEntry(zipFileName));
			IOTools.writeUTF8BOM(zip); // required, because XSLT 1.0 cannot force a BOM, and some tools need it (Excel)
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(xslt));
			// best effort to reduce acute entities, see XSLT files
			transformer.setOutputProperty("{http://xml.apache.org/xalan}entities",
					"org/apache/xml/serializer/XMLEntities");
			transformer.transform(new StreamSource(xml), new StreamResult(zip));
			zip.closeEntry();
		} catch (TransformerFactoryConfigurationError e) {
			LOG.warn("Cannot create new transformer factory", e);
			throw new IOException(e);
		} catch (TransformerConfigurationException e) {
			LOG.warn("Cannot create new transformer", e);
			throw new IOException(e);
		} catch (TransformerException e) {
			LOG.warn("Cannot transform XML", e);
			throw new IOException(e);
		} finally {
			IOTools.ignorantClose(xml, xslt);
			LOG.trace("Transformation done: {}", zipFileName);
		}
	}
}
