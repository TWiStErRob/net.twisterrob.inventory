package net.twisterrob.build;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class TransformTest {

	@Test
	public void transformHtml() throws IOException, TransformerException, SAXException {
		TransformTestProperties props = new TransformTestProperties();
		Transform.main(
				props.getInputXml().getAbsolutePath(),
				props.getXsd().getAbsolutePath(),
				props.getXsltHtml().getAbsolutePath(),
				props.getOutputHtml().getAbsolutePath()
		);
	}

	@Test
	public void transformCsv() throws IOException, TransformerException, SAXException {
		TransformTestProperties props = new TransformTestProperties();
		Transform.main(
				props.getInputXml().getAbsolutePath(),
				props.getXsd().getAbsolutePath(),
				props.getXsltCsv().getAbsolutePath(),
				props.getOutputCsv().getAbsolutePath()
		);
	}
}
