package net.twisterrob.java.io;

import java.io.*;
import java.util.zip.CRC32;

public class CRC32OutputStream extends OutputStream {
	private CRC32 crc = new CRC32();

	@Override public void write(byte[] b) throws IOException {
		crc.update(b);
	}
	@Override public void write(byte[] b, int off, int len) throws IOException {
		crc.update(b, off, len);
	}
	@Override public void write(int b) {
		crc.update(b);
	}

	public long getValue() {
		return crc.getValue();
	}

	public void reset() {
		crc.reset();
	}
}
