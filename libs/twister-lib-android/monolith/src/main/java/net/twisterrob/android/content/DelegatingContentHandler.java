package net.twisterrob.android.content;

import org.xml.sax.*;

import android.support.annotation.*;

public class DelegatingContentHandler implements ContentHandler {
	private static final NullContentHandler NULL_HANDLER = new NullContentHandler();
	@SuppressWarnings("NullableProblems")
	private @NonNull ContentHandler wrapped;

	public DelegatingContentHandler() {
		this(NULL_HANDLER);
	}
	public DelegatingContentHandler(ContentHandler handler) {
		setWrapped(handler);
	}

	public boolean hasWrapped() {
		return wrapped != NULL_HANDLER;
	}
	public @Nullable ContentHandler getWrapped() {
		return hasWrapped()? wrapped : null;
	}
	public void setWrapped(@Nullable ContentHandler handler) {
		wrapped = handler != null? handler : NULL_HANDLER;
	}

	@Override public void setDocumentLocator(Locator locator) {
		wrapped.setDocumentLocator(locator);
	}

	@Override public void startDocument() throws SAXException {
		wrapped.startDocument();
	}
	@Override public void endDocument() throws SAXException {
		wrapped.endDocument();
	}

	@Override public void startPrefixMapping(String prefix, String uri) throws SAXException {
		wrapped.startPrefixMapping(prefix, uri);
	}
	@Override public void endPrefixMapping(String prefix) throws SAXException {
		wrapped.endPrefixMapping(prefix);
	}

	@Override public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		wrapped.startElement(uri, localName, qName, attributes);
	}
	@Override public void endElement(String uri, String localName, String qName) throws SAXException {
		wrapped.endElement(uri, localName, qName);
	}

	@Override public void characters(char[] ch, int start, int length) throws SAXException {
		wrapped.characters(ch, start, length);
	}

	@Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		wrapped.ignorableWhitespace(ch, start, length);
	}

	@Override public void processingInstruction(String target, String data) throws SAXException {
		wrapped.processingInstruction(target, data);
	}

	@Override public void skippedEntity(String name) throws SAXException {
		wrapped.skippedEntity(name);
	}
}
