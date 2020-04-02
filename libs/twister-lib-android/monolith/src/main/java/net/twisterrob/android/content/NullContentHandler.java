package net.twisterrob.android.content;

import org.xml.sax.*;

public class NullContentHandler implements ContentHandler {
	@Override public void setDocumentLocator(Locator locator) {
		// no op
	}

	@Override public void startDocument() {
		// no op
	}

	@Override public void endDocument() {
		// no op
	}

	@Override public void startPrefixMapping(String prefix, String uri) {
		// no op
	}

	@Override public void endPrefixMapping(String prefix) {
		// no op
	}
	@Override public void startElement(String uri, String localName, String qName, Attributes attributes) {
		// no op
	}

	@Override public void endElement(String uri, String localName, String qName) {
		// no op
	}

	@Override public void characters(char[] ch, int start, int length) {
		// no op
	}

	@Override public void ignorableWhitespace(char[] ch, int start, int length) {
		// no op
	}

	@Override public void processingInstruction(String target, String data) {
		// no op
	}

	@Override public void skippedEntity(String name) {
		// no op
	}
}
