package net.twisterrob.inventory.android.backup.xml;

import javax.xml.transform.*;

class MultiCastErrorListener implements ErrorListener {

	private final ErrorListener[] listeners;

	public MultiCastErrorListener(ErrorListener... listeners) {
		this.listeners = listeners;
	}

	@Override public void warning(TransformerException exception) throws TransformerException {
		for (ErrorListener listener : listeners) {
			listener.warning(exception);
		}
	}

	@Override public void error(TransformerException exception) throws TransformerException {
		for (ErrorListener listener : listeners) {
			listener.error(exception);
		}
	}

	@Override public void fatalError(TransformerException exception) throws TransformerException {
		for (ErrorListener listener : listeners) {
			listener.fatalError(exception);
		}
	}
}
