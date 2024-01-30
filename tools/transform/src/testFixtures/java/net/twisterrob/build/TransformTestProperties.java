package net.twisterrob.build;

import java.io.File;

public class TransformTestProperties {
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
		return new File(getOutputDir(), "data-" + getName() + ".html");
	}

	File getXsltCsv() {
		return new File(System.getProperty("net.twisterrob.inventory.transform.xslt.csv"));
	}
	File getOutputCsv() {
		return new File(getOutputDir(), "data-" + getName() + ".csv");
	}
}
