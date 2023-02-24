package net.twisterrob.inventory.android.backup.xml;

import javax.xml.transform.*;

import org.slf4j.*;

import net.twisterrob.inventory.android.backup.BuildConfig;

class StrictXmlErrorListener implements ErrorListener {
	private static final Logger LOG = LoggerFactory.getLogger(StrictXmlErrorListener.class);

	@Override public void warning(TransformerException exception) throws TransformerException {
		if (BuildConfig.DEBUG) {
			throw exception;
		} else {
			LOG.warn("Warning while processing XML", exception);
		}
	}

	@Override public void error(TransformerException exception) throws TransformerException {
		throw exception;
	}

	@Override public void fatalError(TransformerException exception) throws TransformerException {
		throw exception;
	}
}
