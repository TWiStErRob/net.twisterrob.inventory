package net.twisterrob.java.io;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.zip.*;

import javax.annotation.*;

public /*static*/ abstract class IOTools {
	// TODO check if UTF-8 is used by cineworld
	public static final String ENCODING = Charset.forName("UTF-8").name();
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@SuppressWarnings("UnusedReturnValue") // optional convenience value
	public static long copyFile(final String sourceFileName, final String destinationFileName) throws IOException {
		File sourceFile = new File(sourceFileName);
		File destinationFile = new File(destinationFileName);
		return IOTools.copyFile(sourceFile, destinationFile);
	}

	public static void ensure(File dir) throws IOException {
		if (!dir.mkdirs() && (!dir.exists() || !dir.isDirectory())) {
			throw new FileNotFoundException("Failed to ensure directory: " + dir);
		}
	}

	@SuppressWarnings("resource")
	public static long copyFile(final File sourceFile, final File destinationFile) throws IOException {
		ensure(destinationFile.getParentFile());
		InputStream in = new FileInputStream(sourceFile);
		OutputStream out = new FileOutputStream(destinationFile);
		try {
			return IOTools.copyStream(in, out);
		} finally {
			ignorantClose(in, out);
		}
	}

	public static long copyStream(InputStream in, OutputStream out) throws IOException {
		return copyStream(in, out, true);
	}

	public static long copyStream(final InputStream in, final OutputStream out, boolean autoClose) throws IOException {
		try {
			byte[] buf = new byte[16 * 1024];
			long total = 0;
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				total += len;
			}
			out.flush();
			return total;
		} finally {
			ignorantClose(in);
			if (autoClose) {
				ignorantClose(out);
			}
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
	public static byte[] readBytes(File input) throws IOException {
		return readBytes(new FileInputStream(input), input.length());
	}
	public static byte[] readBytes(InputStream input) throws IOException {
		return readBytes(input, 0);
	}
	public static byte[] readBytes(InputStream input, long size) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream((int)size);
		IOTools.copyStream(input, bytes);
		return bytes.toByteArray();
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
		for (Closeable closeMe : closeMes) {
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

	public static void writeAll(OutputStream stream, byte... contents) throws IOException {
		try {
			stream.write(contents);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}

	public static String[] getNames(File... files) {
		String[] names = new String[files.length];
		for (int i = 0; i < files.length; ++i) {
			names[i] = files[i].getName();
		}
		return names;
	}

	/** @see #delete(File, boolean) */
	public static boolean delete(File dir) {
		return delete(dir, false);
	}

	/**
	 * Recursively removes all files and directories.
	 *
	 * @param dir directory to delete (can be a file reference too)
	 * @param stopOnError will cause to stop the recursion on the first error
	 * @return {@code true} if everything was successfully deleted, {@code false} otherwise.
	 * Deleting a {@code null} {@param dir} is considered a failure.
	 *
	 * @see File#delete()
	 */
	public static boolean delete(File dir, boolean stopOnError) {
		boolean result = false;
		if (dir != null) {
			if (!dir.isDirectory()) {
				result = dir.delete();
			} else {
				File[] children = dir.listFiles();
				if (children != null) {
					result = true; // assume success, also covers the case when there are no children
					for (File child : children) {
						result &= delete(child);
						if (!result && stopOnError) {
							break;
						}
					}
				}
			}
		}
		return result;
	}

	public static long calculateSize(File dir) {
		long result = 0;
		if (dir != null) {
			if (!dir.isDirectory()) {
				result = dir.length();
			} else {
				File[] children = dir.listFiles();
				if (children != null) {
					for (File child : children) {
						result += calculateSize(child);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Convenience method to write a full zip file at once.
	 *
	 * @param zipFile the target archive file, existing file will be overwritten
	 * @see #zip(ZipOutputStream, boolean, File...)
	 */
	public static void zip(File zipFile, boolean includeSelf, File... entries) throws IOException {
		ZipOutputStream zip = null;
		try {
			zip = new ZipOutputStream(new FileOutputStream(zipFile));
			zip(zip, includeSelf, entries);
		} finally {
			ignorantClose(zip);
		}
	}

	/**
	 * Convenience method to write multiple files/directories into the zip file.
	 *
	 * @param zipOut target zip file stream
	 * @see #zip(ZipOutputStream, boolean, File)
	 */
	public static void zip(ZipOutputStream zipOut, boolean includeSelf, File... entries) throws IOException {
		for (File entry : entries) {
			zip(zipOut, includeSelf, entry);
		}
	}

	/**
	 * Writes a directory recursively or a file to the zip file.
	 *
	 * @param zipOut target zip file stream
	 * @param includeSelf whether to include the folder or just its contents
	 * @param entry file or folder
	 */
	public static void zip(ZipOutputStream zipOut, boolean includeSelf, File entry) throws IOException {
		if (includeSelf || !entry.isDirectory()) {
			addToZip(zipOut, "", entry.getParentFile(), entry);
		} else {
			addChildren(zipOut, "", entry, entry);
		}
	}

	/**
	 * Writes a file's contents to the zip file. If the file is a directory only the entry is created.
	 *  @param zipOut target zip file stream
	 * @param zipRelativePath path and name of the entry in the zip file
	 * @param entry file or folder
	 */
	public static void zip(ZipOutputStream zipOut, String zipRelativePath, File entry) throws IOException {
		ZipEntry zipEntry = new ZipEntry(zipRelativePath);
		zipEntry.setTime(entry.lastModified());
		zipOut.putNextEntry(zipEntry);
		if (!entry.isDirectory()) {
			copyStream(new FileInputStream(entry), zipOut, false);
		}
		zipOut.closeEntry();
	}

	/**
	 * Writes an InputStream's contents to the zip file.
	 *  @param zipOut target zip file stream
	 * @param zipRelativePath path and name of the entry in the zip file
	 * @param entry file or folder
	 */
	public static void zip(ZipOutputStream zipOut, String zipRelativePath, InputStream entry) throws IOException {
		ZipEntry zipEntry = new ZipEntry(zipRelativePath);
		zipOut.putNextEntry(zipEntry);
		copyStream(entry, zipOut, false);
		zipOut.closeEntry();
	}

	/**
	 * Adds a file or directory to the zip file inside the specified subdirectory.
	 *
	 * @param zipOut target zip file stream
	 * @param subDir relative parent path of the entry
	 */
	public static void zip(ZipOutputStream zipOut, File entry, String subDir) throws IOException {
		if (!subDir.endsWith("/")) {
			subDir += "/";
		}
		if (entry.isDirectory()) {
			addChildren(zipOut, subDir, entry, entry);
		} else {
			addToZip(zipOut, subDir, entry.getParentFile(), entry);
		}
	}

	/**
	 * Adds an a file or dir to the zip file inside the specified subdirectory.
	 *
	 * @param zipOut target zip file stream
	 * @param subDir relative parent path of the entry inside the zip
	 * @param rootDir the original root folder of the source files
	 * @param entry the current entry inside the root folder
	 * @throws IOException if something fails
	 */
	private static void addToZip(ZipOutputStream zipOut, String subDir, File rootDir, File entry) throws IOException {
		String relativePath = rootDir.toURI().relativize(entry.toURI()).getPath();
		zip(zipOut, subDir + relativePath, entry);
		if (entry.isDirectory()) {
			addChildren(zipOut, subDir, rootDir, entry);
		}
	}
	/**
	 * Adds a folder's contents to the zip file inside the specified subdirectory.
	 *
	 * @param zipOut target zip file stream
	 * @param subDir relative parent path of the entry inside the zip
	 * @param rootDir the original root folder of the source files
	 * @param dir the current entry inside the root folder
	 */
	private static void addChildren(ZipOutputStream zipOut, String subDir, File rootDir, File dir) throws IOException {
		File[] children = dir.listFiles();
		if (children != null) {
			for (File child : children) {
				addToZip(zipOut, subDir, rootDir, child);
			}
		} else {
			throw new IOException("Cannot read directory " + dir);
		}
	}

	@SuppressWarnings("RedundantThrows") // keep it consistent
	public static long crc(byte... arr) throws IOException {
		CRC32 crc = new CRC32();
		crc.update(arr);
		return crc.getValue();
	}

	public static long crc(File file) throws IOException {
		CRC32OutputStream crc = new CRC32OutputStream();
		IOTools.copyStream(new FileInputStream(file), crc, true);
		return crc.getValue();
	}

	public static InputStream stream(String string) throws IOException {
		return new ByteArrayInputStream(string.getBytes("UTF-8"));
	}

	public static void store(@Nonnull ZipOutputStream zip,
			@Nonnull File file, @Nullable String comment) throws IOException {
		InputStream imageFile = new FileInputStream(file);
		ZipEntry entry = new ZipEntry(file.getName());
		entry.setTime(file.lastModified());
		entry.setMethod(ZipEntry.STORED);
		entry.setSize(file.length());
		entry.setCrc(IOTools.crc(file));
		entry.setComment(comment);
		try {
			zip.putNextEntry(entry);
			IOTools.copyStream(imageFile, zip, false);
			zip.closeEntry();
		} finally {
			IOTools.ignorantClose(imageFile);
		}
	}

	public static void store(@Nonnull ZipOutputStream zip,
			@Nonnull String name, @Nonnull byte[] contents, long epoch, @Nullable String comment) throws IOException {
		ZipEntry entry = new ZipEntry(name);
		entry.setTime(epoch);
		entry.setMethod(ZipEntry.STORED);
		entry.setSize(contents.length);
		entry.setCrc(IOTools.crc(contents));
		entry.setComment(comment);

		zip.putNextEntry(entry);
		zip.write(contents);
		zip.closeEntry();
	}

	protected IOTools() {
		// static utility class
	}
	public static boolean isValidDir(File dir) {
		return dir != null && dir.isDirectory() && dir.exists();
	}
	public static boolean isValidFile(File file) {
		return file != null && file.isFile() && file.exists();
	}

	public static void writeUTF8BOM(OutputStream out) throws IOException {
		out.write(0xEF);
		out.write(0xBB);
		out.write(0xBF);
	}
}
