package net.twisterrob.inventory.android.content.io.csv;

import java.io.*;

import org.apache.commons.csv.*;

import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.content.io.DatabaseImporter;

public class DatabaseCSVImporter extends DatabaseImporter {
	public void importAll(InputStream in) throws IOException {
		@SuppressWarnings("resource")
		CSVParser parser = CSVConstants.FORMAT.parse(new InputStreamReader(in, CSVConstants.ENCODING));
		try {
			startProcess();
			for (CSVRecord record : parser) {
				String type = record.get("type");
				String property = record.get("property");
				String room = record.get("room");
				String item = record.get("item");
				String image = record.get("image");
				String parent = record.get("parent");
				String id = record.get("id");

				process(type, property, room, item, image, parent, id);
			}
			endProcess(null);
		} catch (RuntimeException ex) {
			endProcess(ex);
		} finally {
			IOTools.ignorantClose(parser);
		}
	}

	protected void process(String type, String property, String room, String item, String image, String parent,
			String id) {
		DriveId driveId = image != null? DriveId.decodeFromString(image) : null;

		if (item != null) { // item: property ?= null && room ?= null && item != null
			processItem(type, property, room, item, parent, id, driveId);
		} else if (room != null) { // room: property ?= null && room != null && item == null
			processRoom(type, property, room, driveId);
		} else if (property != null) { // property: property != null && room == null && item == null
			processProperty(type, property, driveId);
		} else {
			throw new IllegalArgumentException("Cannot identify entity: property, room or item must be set"
					+ ", type=" + type + ", image=" + image + ", parent=" + parent + ", id=" + id);
		}
	}
}
