package net.twisterrob.android.content;

import org.xml.sax.*;

public class NullContentHandler implements ContentHandler {
	@Override public void setDocumentLocator(Locator locator) {
		// no op
	}

	@Override public void startDocument() throws SAXException {
		// no op
	}

	@Override public void endDocument() throws SAXException {
		// no op
	}

	@Override public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// no op
	}

	@Override public void endPrefixMapping(String prefix) throws SAXException {
		// no op
	}
	@Override public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		// no op
	}

	@Override public void endElement(String uri, String localName, String qName) throws SAXException {
		// no op
	}

	@Override public void characters(char[] ch, int start, int length) throws SAXException {
		// no op
	}

	@Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// no op
	}

	@Override public void processingInstruction(String target, String data) throws SAXException {
		// no op
	}

	@Override public void skippedEntity(String name) throws SAXException {
		// no op
	}
}
