package net.twisterrob.inventory.android.content.io.xml;

import java.io.*;
import java.util.*;

import org.slf4j.*;
import org.xmlpull.v1.XmlSerializer;

import android.database.Cursor;
import android.util.Xml;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.contract.ParentColumns.Type;
import net.twisterrob.inventory.android.content.io.*;
import net.twisterrob.java.utils.StringTools;

public class ZippedXMLExporter extends ZippedExporter<XmlSerializer> {
	private static final Logger LOG = LoggerFactory.getLogger(ZippedXMLExporter.class);
	public static final String NS = "";
	public static final String TAG_ROOT = "inventory";
	public static final String TAG_PROPERTY = "property";
	public static final String TAG_ROOM = "room";
	public static final String TAG_ITEM = "item";
	public static final String TAG_LIST = "list";
	public static final String TAG_ITEM_REF = "item-ref";
	public static final String ATTR_ID = "id";
	private Hierarchy hier;

	public ZippedXMLExporter() {
		super(Paths.BACKUP_XML_FILENAME);
	}

	@Override public void initExport(OutputStream os) {
		super.initExport(os);
		hier = new Hierarchy();
	}
	@Override protected XmlSerializer initData(OutputStream dataStream, Cursor cursor) throws IOException {
		XmlSerializer serializer = Xml.newSerializer();
		serializer.setOutput(dataStream, "utf-8");
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		serializer.startDocument("utf-8", true);
		serializer.startTag(NS, TAG_ROOT);
		serializer.attribute(NS, "approximateCount", String.valueOf(cursor.getCount()));
		return serializer;
	}

	@Override protected void writeData(XmlSerializer output, Cursor cursor) throws IOException {
		long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
		long parentID = cursor.getLong(cursor.getColumnIndexOrThrow(Item.PARENT_ID));
		Type type = Type.from(cursor, "type");

		Belonging belonging = hier.get(type, id);
		belonging.name = cursor.getString(cursor.getColumnIndexOrThrow(nameColumn(type)));
		belonging.type = cursor.getString(cursor.getColumnIndexOrThrow("typeName"));
		belonging.image = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.IMAGE));
		belonging.description = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.DESCRIPTION));
		belonging.comment = ExporterTask.buildComment(cursor);

		if (type != Type.Property) { // can't have parents
			hier.put(parentID, Type.from(cursor, "parentType"), belonging);
		}
	}

	@Override protected void finishData(XmlSerializer output, Cursor cursor) throws IOException {
		hier.write(output);
		writeLists(output);
		output.endTag(NS, TAG_ROOT);
		output.endDocument();
		output.flush();
	}

	private void writeLists(XmlSerializer output) throws IOException {
		Cursor listsCursor = App.db().listLists(Item.ID_ADD);
		while (listsCursor.moveToNext()) {
			output.startTag(NS, TAG_LIST);
			output.attribute(NS, "name", listsCursor.getString(listsCursor.getColumnIndex(CommonColumns.NAME)));
			Cursor list = App.db().listItemsInList(listsCursor.getLong(listsCursor.getColumnIndex(CommonColumns.ID)));
			while (list.moveToNext()) {
				output.startTag(NS, TAG_ITEM_REF);
				long itemID = list.getLong(list.getColumnIndex(CommonColumns.ID));
				String itemName = list.getString(list.getColumnIndex(CommonColumns.NAME));
				output.attribute(NS, ATTR_ID, String.valueOf(itemID));
				output.endTag(NS, TAG_ITEM_REF);
				output.comment(itemName);
			}
			list.close();
			output.endTag(NS, TAG_LIST);
		}
		listsCursor.close();
	}

	public static String nameColumn(Type type) {
		switch (type) {
			case Property:
				return Item.PROPERTY_NAME;
			case Room:
				return Item.ROOM_NAME;
			case Item:
				return "itemName";
		}
		throw new IllegalStateException("Unknown type: " + type);
	}

	private abstract static class Belonging<T extends Belonging> {
		long id;
		String name;
		String type;
		String image;
		String description;
		String comment;
		List<T> children = new ArrayList<>();

		void write(XmlSerializer output) throws IOException {
			String tag = getTag();
			output.startTag(NS, tag);
			output.attribute(NS, ATTR_ID, String.valueOf(id));
			output.attribute(NS, "name", name);
			output.attribute(NS, "type", type);
			if (image != null) {
				output.attribute(NS, "image", image);
			}
			boolean hasBody = !children.isEmpty() || !StringTools.isNullOrEmpty(description);
			if (hasBody) {
				output.comment(comment);
			}
			if (!StringTools.isNullOrEmpty(description)) {
				output.startTag(NS, "description");
				output.text(description);
				output.endTag(NS, "description");
			}
			for (T child : children) {
				child.write(output);
			}
			output.endTag(NS, tag);
			if (!hasBody) {
				output.comment(comment);
			}
		}
		protected abstract String getTag();
	}

	private static class XProperty extends Belonging<XRoom> {
		@Override protected String getTag() {
			return TAG_PROPERTY;
		}
	}

	private static class XRoom extends Belonging<XItem> {
		@Override protected String getTag() {
			return TAG_ROOM;
		}
	}

	private static class XItem extends Belonging<XItem> {
		@Override protected String getTag() {
			return TAG_ITEM;
		}
	}

	private static class Hierarchy {
		private Map<Long, XProperty> properties = new TreeMap<>();
		private Map<Long, XRoom> rooms = new TreeMap<>();
		private Map<Long, XItem> items = new TreeMap<>();

		private void put(long parentID, Type parentType, Belonging<?> belonging) {
			switch (parentType) {
				case Property: {
					XProperty parent = getOrCreateProperty(parentID);
					parent.children.add((XRoom)belonging);
					break;
				}
				case Room: {
					XRoom parent = getOrCreateRoom(parentID);
					parent.children.add((XItem)belonging);
					break;
				}
				case Item: {
					XItem parent = getOrCreateItem(parentID);
					parent.children.add((XItem)belonging);
					break;
				}
			}
		}

		public Belonging get(Type type, long id) {
			switch (type) {
				case Property:
					return getOrCreateProperty(id);
				case Room:
					return getOrCreateRoom(id);
				case Item:
					return getOrCreateItem(id);
			}
			throw new IllegalStateException("Unknown type: " + type);
		}

		private XProperty getOrCreateProperty(long id) {
			XProperty x = properties.get(id);
			if (x == null) {
				x = new XProperty();
				x.id = id;
				properties.put(x.id, x);
			}
			return x;
		}

		private XRoom getOrCreateRoom(long id) {
			XRoom x = rooms.get(id);
			if (x == null) {
				x = new XRoom();
				x.id = id;
				rooms.put(x.id, x);
			}
			return x;
		}

		private XItem getOrCreateItem(long id) {
			XItem x = items.get(id);
			if (x == null) {
				x = new XItem();
				x.id = id;
				items.put(x.id, x);
			}
			return x;
		}

		public void write(XmlSerializer output) throws IOException {
			for (XProperty property : properties.values()) {
				property.write(output);
			}
		}
	}
}
