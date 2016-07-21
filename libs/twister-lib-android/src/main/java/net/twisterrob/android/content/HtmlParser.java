package net.twisterrob.android.content;

import java.util.ArrayDeque;

import org.xml.sax.*;

import android.text.*;
import android.text.Html.ImageGetter;

/**
 * @see <a href="http://stackoverflow.com/a/36548274/253468">How to read custom html tag attributes using Android TagHandler</a>
 * @see <a href="http://stackoverflow.com/a/36528149/253468">How to get an attribute from an XMLReader</a>
 */
public class HtmlParser extends DelegatingContentHandler implements Html.TagHandler {
	public interface TagHandler {
		/**
		 * @param attributes spaces and signs may mess up attributes, be careful
		 */
		boolean handleTag(boolean opening, String tag, Editable output, Attributes attributes);
	}

	@SuppressWarnings("deprecation")
	public static Spanned fromHtml(String html, ImageGetter imageGetter, TagHandler handler) {
		// add a tag at the start that is not handled by default,
		// allowing custom tag handler to replace xmlReader contentHandler
		return Html.fromHtml("<inject/>" + html, imageGetter, new HtmlParser(handler));
	}

	public static String getValue(Attributes attributes, String name) {
		for (int i = 0, n = attributes.getLength(); i < n; i++) {
			if (name.equals(attributes.getLocalName(i))) {
				return attributes.getValue(i);
			}
		}
		return null;
	}

	private final TagHandler handler;
	private final ArrayDeque<Boolean> tagStatus = new ArrayDeque<>();
	private Editable output;

	private HtmlParser(TagHandler handler) {
		this.handler = handler;
	}

	@Override public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
		if (!hasWrapped()) {
			this.output = output;
			setWrapped(xmlReader.getContentHandler());
			xmlReader.setContentHandler(this);

			// handle endElement() callback for <inject/> tag
			tagStatus.addLast(Boolean.FALSE);
		}
	}

	@Override public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		boolean isHandled = handler.handleTag(true, localName, output, attributes);
		tagStatus.addLast(isHandled);
		if (!isHandled) {
			super.startElement(uri, localName, qName, attributes);
		}
	}

	@Override public void endElement(String uri, String localName, String qName) throws SAXException {
		if (!tagStatus.removeLast()) {
			super.endElement(uri, localName, qName);
		}
		handler.handleTag(false, localName, output, null);
	}
}
