package net.twisterrob.inventory.android.backup;

import java.io.*;
import java.util.*;
import java.util.zip.ZipOutputStream;

import com.shazam.gwen.collaborators.Arranger;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.Constants;

public class BackupZip implements Arranger {
	private final ZipOutputStream preparedZip;
	private final PipedInputStream preparedInput;
	private final List<String> images = new ArrayList<>();
	private final List<Item> items = new ArrayList<>();
	private boolean hasXML;

	public BackupZip() throws IOException {
		PipedOutputStream preparingOutput = new PipedOutputStream();
		preparedInput = new PipedInputStream();
		preparingOutput.connect(preparedInput);

		preparedZip = new ZipOutputStream(preparingOutput);
	}

	public BackupZip withFile(String name, byte[] contents) throws Throwable {
		IOTools.zip(preparedZip, name, new ByteArrayInputStream(contents));
		return this;
	}

	public BackupZip withDataXML() throws Throwable {
		hasXML = true;
		withFile(Constants.Paths.BACKUP_DATA_FILENAME, "xml".getBytes("utf-8"));
		return this;
	}

	public BackupZip withImages(String... imageNames) throws Throwable {
		for (String imageName : imageNames) {
			withImage(imageName);
		}
		return this;
	}
	public BackupZip withImage(String imageName) throws Throwable {
		images.add(imageName);
		withFile(imageName, getContents(imageName));
		return this;
	}

	public static byte[] getContents(String imageName) throws UnsupportedEncodingException {
		return (imageName + " contents").getBytes("utf-8");
	}
	static String getName(byte[] contents) throws UnsupportedEncodingException {
		String full = new String(contents, "utf-8");
		return full.substring(0, full.length() - " contents".length());
	}

	boolean hasXML() {
		return hasXML;
	}
	List<String> getImages() {
		return Collections.unmodifiableList(images);
	}
	List<Item> getItems() {
		return Collections.unmodifiableList(items);
	}
	public InputStream getStream() throws IOException {
		preparedZip.close();
		return preparedInput;
	}
	public BackupZip containing(int id, String name, String image) {
		Item item = new Item(id, name, image);
		items.add(item);
		return this;
	}

	static class Item {
		final int id;
		final String name;
		final String image;
		public Item(int id, String name, String image) {
			this.id = id;
			this.name = name;
			this.image = image;
		}
	}
}
