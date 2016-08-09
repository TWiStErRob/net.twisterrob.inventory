package net.twisterrob.inventory.android.content.io.xml;

import java.io.*;
import java.util.zip.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.slf4j.*;

import android.content.res.AssetManager;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.content.io.ZippedExporter;
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
		OutputStream out = super.startStream(zip);
		return new TeeOutputStream(out, capturedXML);
	}

	@Override protected void endStream() throws IOException {
		byte[] xml = capturedXML.toByteArray();
		super.endStream();
		AssetManager assets = App.getAppContext().getAssets();
		copyXSLT(assets.open("data.html.xslt"));
		transform(HTML_NAME, new ByteArrayInputStream(xml), assets.open("data.html.xslt"));
		transform(CSV_NAME, new ByteArrayInputStream(xml), assets.open("data.csv.xslt"));
	}

	private void copyXSLT(InputStream xsltStream) throws IOException {
		try {
			zip.putNextEntry(new ZipEntry(XSLT_NAME));
			IOTools.copyStream(xsltStream, zip, false);
			zip.closeEntry();
		} finally {
			IOTools.ignorantClose(xsltStream);
		}
	}

	private void transform(String filename, InputStream xml, InputStream xslt) throws IOException {
		try {
			zip.putNextEntry(new ZipEntry(filename));
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
		}
	}
}
