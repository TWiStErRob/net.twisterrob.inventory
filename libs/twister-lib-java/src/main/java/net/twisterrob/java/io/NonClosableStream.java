package net.twisterrob.java.io;

import java.io.*;

public class NonClosableStream extends FilterInputStream {
	private boolean closeAttempted;
	public NonClosableStream(InputStream in) {
		super(in);
	}

	public boolean isCloseAttempted() {
		return closeAttempted;
	}

	@Override public void close() {
		closeAttempted = true;
		//super.close(); // don't allow it
	}

	public void doClose() throws IOException {
		super.close();
	}
}
