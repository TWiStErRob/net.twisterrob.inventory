package net.twisterrob.java.io;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

public/* static */class IOTools {
	// TODO check if UTF-8 is used by cineworld
	public static final String ENCODING = Charset.forName("UTF-8").name();
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static int copyFile(final String sourceFileName, final String destinationFileName) throws IOException {
		File sourceFile = new File(sourceFileName);
		File destinationFile = new File(destinationFileName);
		return IOTools.copyFile(sourceFile, destinationFile);
	}

	@SuppressWarnings("resource")
	public static int copyFile(final File sourceFile, final File destinationFile) throws IOException {
		destinationFile.getParentFile().mkdirs();
		InputStream in = new FileInputStream(sourceFile);
		OutputStream out = new FileOutputStream(destinationFile);
		int totalBytes;
		try {
			totalBytes = IOTools.copyStream(in, out);
		} finally {
			ignorantClose(in, out);
		}
		return totalBytes;
	}

	public static int copyStream(final InputStream in, final OutputStream out) throws IOException {
		try {
			byte[] buf = new byte[4096];
			int total = 0;
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				total += len;
			}
			return total;
		} finally {
			ignorantClose(in, out);
		}
	}

	public static String readAll(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int c = reader.read(); c != -1; c = reader.read()) {
			sb.append((char)c);
		}
		return sb.toString();
	}
	public static String readAll(InputStream stream) throws IOException {
		return readAll(new InputStreamReader(stream, ENCODING));
	}
	public static String readAll(InputStream stream, String charsetName) throws IOException {
		return readAll(new InputStreamReader(stream, charsetName));
	}

	public static void ignorantClose(Closeable closeMe) {
		if (closeMe != null) {
			try {
				closeMe.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static void ignorantClose(Closeable... closeMes) {
		if (closeMes == null) {
			return;
		}
		for (Closeable closeMe: closeMes) {
			ignorantClose(closeMe);
		}
	}

	public static void closeConnection(HttpURLConnection connection, Closeable... resources) {
		IOTools.ignorantClose(resources);
		if (connection != null) {
			connection.disconnect();
		}
	}

	public static void writeAll(OutputStream stream, String contents) throws IOException {
		try {
			writeAll(stream, contents.getBytes(ENCODING));
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static void writeAll(OutputStream stream, byte[] contents) throws IOException {
		try {
			stream.write(contents);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}
}
