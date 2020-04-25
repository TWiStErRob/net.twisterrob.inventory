package net.twisterrob.android.content.glide;

import java.io.*;
import java.lang.reflect.*;

import javax.xml.parsers.*;

import org.xml.sax.*;

import android.os.Build;
import android.support.annotation.NonNull;

import com.caverock.androidsvg.*;

/**
 * @see "src/main/consumer.pro"
 */
public class SVGWorkarounds {
	/**
	 * Workaround for <a href="https://github.com/BigBadaboom/androidsvg/issues/193">
	 *     "New" XML parsing doesn't work on API 10</a>
	 */
	// === SVG.getFromInputStream(source) - parseUsingXmlPullParser try
	static @NonNull SVG getFromInputStream(InputStream source) throws SVGParseException {
		if (Build.VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT) {
			return SVG.getFromInputStream(source);
		}
		try {
			Class<?> SVGParser = Class.forName("com.caverock.androidsvg.SVGParser");
			Constructor<?> ctor = SVGParser.getDeclaredConstructor();
			ctor.setAccessible(true);
			Object parser = ctor.newInstance();
			Field svgDocument = SVGParser.getDeclaredField("svgDocument");
			svgDocument.setAccessible(true);
			parseUsingSAX(parser, source);
			return (SVG)svgDocument.get(parser);
		} catch (SVGParseException ex) {
			throw ex;
		} catch (NoSuchFieldException ex) {
			throw newSVGParseException("Error hacking SVG", ex);
		} catch (IllegalAccessException ex) {
			throw newSVGParseException("Error hacking SVG", ex);
		} catch (InstantiationException ex) {
			throw newSVGParseException("Error hacking SVG", ex);
		} catch (InvocationTargetException ex) {
			throw newSVGParseException("Error hacking SVG", ex);
		} catch (ClassNotFoundException ex) {
			throw newSVGParseException("Error hacking SVG", ex);
		} catch (NoSuchMethodException ex) {
			throw newSVGParseException("Error hacking SVG", ex);
		} catch (ParserConfigurationException ex) {
			throw newSVGParseException("Error hacking SVG", ex);
		} catch (IOException ex) {
			throw newSVGParseException("Error hacking SVG", ex);
		} catch (SAXException ex) {
			throw newSVGParseException("Error hacking SVG", ex);
		}
	}

	// === SVGParser.parseUsingSAX - setFeature calls
	private static void parseUsingSAX(Object parser, InputStream is) throws
			ParserConfigurationException, SAXException, IOException,
			ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, InstantiationException {
		SAXParserFactory spf = SAXParserFactory.newInstance();

		if (Build.VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT) {
			spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		}

		SAXParser sp = spf.newSAXParser();
		XMLReader xr = sp.getXMLReader();

		Class<?> SAXHandler = Class.forName("com.caverock.androidsvg.SVGParser$SAXHandler");
		Constructor<?> ctor = SAXHandler.getDeclaredConstructor(parser.getClass());
		ctor.setAccessible(true); // private inner class
		ContentHandler handler = (ContentHandler)ctor.newInstance(parser);
		xr.setContentHandler(handler);
		xr.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

		xr.parse(new InputSource(is));
	}

	// === new SVGParseException(message, ex)
	private static @NonNull SVGParseException newSVGParseException(String message, Exception ex) {
		try {
			Constructor<SVGParseException> exCtor =
					SVGParseException.class.getDeclaredConstructor(String.class, Exception.class);
			exCtor.setAccessible(true); // package private constructor
			return exCtor.newInstance(message, ex);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}
}
