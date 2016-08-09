package net.twisterrob.java.io;

import java.io.*;

import javax.annotation.Nonnull;

public class TeeOutputStream extends OutputStream {
	private final OutputStream real;
	private final OutputStream steal;

	public TeeOutputStream(@Nonnull OutputStream real, @Nonnull OutputStream steal) {
		this.real = real;
		this.steal = steal;
	}

	public void write(@Nonnull byte[] b) throws IOException {
		real.write(b);
		steal.write(b);
	}

	public void write(@Nonnull byte[] b, int off, int len) throws IOException {
		real.write(b, off, len);
		steal.write(b, off, len);
	}

	public void write(int b) throws IOException {
		real.write(b);
		steal.write(b);
	}

	public void flush() throws IOException {
		real.flush();
		steal.flush();
	}

	public void close() throws IOException {
		try {
			flush();
		} finally {
			try {
				real.close();
			} finally {
				steal.close();
			}
		}
	}
}
