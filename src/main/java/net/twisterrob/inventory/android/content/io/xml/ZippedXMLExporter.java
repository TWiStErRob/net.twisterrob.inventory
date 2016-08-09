package net.twisterrob.inventory.android.content.io.xml;

import java.io.*;
import java.util.zip.*;

import org.slf4j.*;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.content.io.ZippedExporter;
import net.twisterrob.java.io.TeeOutputStream;

public class ZippedXMLExporter extends ZippedExporter {
	private static final Logger LOG = LoggerFactory.getLogger(ZippedXMLExporter.class);

	public static final String XML_NAME = Paths.BACKUP_DATA_FILENAME;
	public static final String XSLT_NAME = Paths.BACKUP_DATA_FILENAME + ".xslt";
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
		super.endStream();
		copyXSLT(App.getAppContext().getAssets().open("data.html.xslt"));
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
}
