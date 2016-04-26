package net.twisterrob.inventory.android.content.io.xml;

import java.io.*;
import java.util.*;

import org.slf4j.*;
import org.xmlpull.v1.XmlSerializer;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Xml;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.io.*;
import net.twisterrob.inventory.android.content.model.HierarchyBuilder;
import net.twisterrob.java.utils.StringTools;

public class ZippedXMLExporter extends ZippedExporter<XmlSerializer> {
	private static final Logger LOG = LoggerFactory.getLogger(ZippedXMLExporter.class);
	public static final String NS = "";
	public static final String TAG_ROOT = "inventory";
	public static final String ATTR_COUNT = "approximateCount";
	public static final String ATTR_ID = "id";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_IMAGE = "image";
	public static final String TAG_PROPERTY = "property";
	public static final String TAG_ROOM = "room";
	public static final String TAG_ITEM = "item";
	public static final String TAG_LIST = "list";
	public static final String TAG_DESCRIPTION = "description";
	public static final String TAG_ITEM_REF = "item-ref";
	public static final String ENCODING = "utf-8";

	private Hierarchy hier;

	public ZippedXMLExporter() {
		super(Paths.BACKUP_DATA_FILENAME);
	}

	@Override public void initExport(OutputStream os) {
		super.initExport(os);
		hier = new Hierarchy();
	}
	@Override protected XmlSerializer initData(OutputStream dataStream, Cursor cursor) throws IOException {
		XmlSerializer serializer = Xml.newSerializer();
		serializer.setOutput(dataStream, ENCODING);
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		serializer.startDocument(ENCODING, true);
		serializer.startTag(NS, TAG_ROOT);
		serializer.attribute(NS, ATTR_COUNT, String.valueOf(cursor.getCount()));
		return serializer;
	}

	@Override protected void writeData(XmlSerializer output, Cursor cursor) throws IOException {
		long id = DatabaseTools.getLong(cursor, CommonColumns.ID);
		long parentID = DatabaseTools.getLong(cursor, ParentColumns.PARENT_ID);
		Type type = Type.from(cursor, CommonColumns.TYPE);

		Belonging<?> belonging = hier.getOrCreate(type, id);
		belonging.name = DatabaseTools.getString(cursor, nameColumn(type));
		belonging.type = DatabaseTools.getString(cursor, "typeName");
		belonging.image = DatabaseTools.getString(cursor, ExporterTask.IMAGE_NAME);
		belonging.description = DatabaseTools.getString(cursor, CommonColumns.DESCRIPTION);
		if (BuildConfig.DEBUG) {
			belonging.comment = ExporterTask.buildComment(cursor);
		}

		if (type != Type.Property) { // can't have parents
			hier.put(Type.from(cursor, ParentColumns.PARENT_TYPE), parentID, belonging);
		}
	}

	@Override protected void finishData(XmlSerializer output, Cursor cursor) throws IOException {
		hier.write(output);
		writeLists(output);
		output.endTag(NS, TAG_ROOT);
		output.endDocument();
		output.flush();
	}

	@SuppressWarnings({"resource", "TryFinallyCanBeTryWithResources"})
	private void writeLists(XmlSerializer output) throws IOException {
		Cursor lists = App.db().listLists(Item.ID_ADD);
		try {
			while (lists.moveToNext()) {
				writeList(output, lists);
			}
		} finally {
			lists.close();
		}
	}
	@SuppressWarnings({"resource", "TryFinallyCanBeTryWithResources"})
	private void writeList(XmlSerializer output, Cursor list) throws IOException {
		long listID = DatabaseTools.getLong(list, CommonColumns.ID);
		String listName = DatabaseTools.getString(list, CommonColumns.NAME);
		output.startTag(NS, TAG_LIST);
		output.attribute(NS, ATTR_NAME, listName);
		Cursor item = App.db().listItemsInList(listID);
		try {
			while (item.moveToNext()) {
				output.startTag(NS, TAG_ITEM_REF);
				long itemID = DatabaseTools.getLong(item, CommonColumns.ID);
				String itemName = DatabaseTools.getString(item, CommonColumns.NAME);
				output.attribute(NS, ATTR_ID, String.valueOf(itemID));
				output.endTag(NS, TAG_ITEM_REF);
				output.comment(itemName);
			}
			output.endTag(NS, TAG_LIST);
		} finally {
			item.close();
		}
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

	private abstract static class Belonging<T extends Belonging<?>> {
		long id;
		String name;
		String type;
		String image;
		String description;
		String comment;
		final List<T> children = new ArrayList<>();

		void write(XmlSerializer output) throws IOException {
			String tag = getTag();
			output.startTag(NS, tag);
			output.attribute(NS, ATTR_ID, String.valueOf(id));
			output.attribute(NS, ATTR_NAME, name);
			output.attribute(NS, ATTR_TYPE, type);
			if (image != null) {
				output.attribute(NS, ATTR_IMAGE, image);
			}
			boolean hasBody = !children.isEmpty() || !StringTools.isNullOrEmpty(description);
			if (hasBody && !TextUtils.isEmpty(comment)) {
				output.comment(comment);
			}
			if (!StringTools.isNullOrEmpty(description)) {
				output.startTag(NS, TAG_DESCRIPTION);
				output.text(description);
				output.endTag(NS, TAG_DESCRIPTION);
			}
			for (T child : children) {
				child.write(output);
			}
			output.endTag(NS, tag);
			if (!hasBody && !TextUtils.isEmpty(comment)) {
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

	private static class Hierarchy extends HierarchyBuilder<Belonging<?>, XProperty, XRoom, XItem> {
		@Override protected void addPropertyChild(@NonNull XProperty parentProperty, @NonNull XRoom childRoom) {
			parentProperty.children.add(childRoom);
		}
		@Override protected void addRoomChild(@NonNull XRoom parentRoom, @NonNull XItem childItem) {
			parentRoom.children.add(childItem);
		}
		@Override protected void addItemChild(@NonNull XItem parentItem, @NonNull XItem childItem) {
			parentItem.children.add(childItem);
		}

		@Override protected @NonNull XProperty createProperty(long id) {
			XProperty property = new XProperty();
			property.id = id;
			return property;
		}

		@Override protected @NonNull XRoom createRoom(long id) {
			XRoom room = new XRoom();
			room.id = id;
			return room;
		}

		@Override protected @NonNull XItem createItem(long id) {
			XItem item = new XItem();
			item.id = id;
			return item;
		}

		public void write(XmlSerializer output) throws IOException {
			for (XProperty property : getAllProperties()) {
				property.write(output);
			}
		}
	}
}
