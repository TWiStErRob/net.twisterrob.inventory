package net.twisterrob.build;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class TransformTest {

	@Test
	public void transformHtml() throws IOException, TransformerException, SAXException {
		Props props = new Props();
		Transform.main(
				props.getInputXml().getAbsolutePath(),
				props.getXsd().getAbsolutePath(),
				props.getXsltHtml().getAbsolutePath(),
				props.getOutputHtml().getAbsolutePath()
		);
	}

	@Test
	public void transformCsv() throws IOException, TransformerException, SAXException {
		Props props = new Props();
		Transform.main(
				props.getInputXml().getAbsolutePath(),
				props.getXsd().getAbsolutePath(),
				props.getXsltCsv().getAbsolutePath(),
				props.getOutputCsv().getAbsolutePath()
		);
	}
}

class Props {
	File getInputXml() {
		return new File(System.getProperty("net.twisterrob.inventory.transform.xml"));
	}
	File getXsd() {
		return new File(System.getProperty("net.twisterrob.inventory.transform.xsd"));
	}

	File getName() {
		return new File(System.getProperty("net.twisterrob.inventory.transform.name"));
	}
	File getOutputDir() {
		return new File(System.getProperty("net.twisterrob.inventory.transform.output"));
	}

	File getXsltHtml() {
		return new File(System.getProperty("net.twisterrob.inventory.transform.xslt.html"));
	}
	File getOutputHtml() {
		return new File(getOutputDir(), "data-" + getName() + ".csv");
	}

	File getXsltCsv() {
		return new File(System.getProperty("net.twisterrob.inventory.transform.xslt.csv"));
	}
	File getOutputCsv() {
		return new File(getOutputDir(), "data-" + getName() + ".csv");
	}
}
