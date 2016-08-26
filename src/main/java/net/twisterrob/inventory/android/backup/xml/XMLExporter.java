package net.twisterrob.inventory.android.backup.xml;

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
import net.twisterrob.inventory.android.backup.exporters.BackupStreamExporter;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.HierarchyBuilder;
import net.twisterrob.java.utils.StringTools;

public class XMLExporter implements CursorExporter {
	private static final Logger LOG = LoggerFactory.getLogger(XMLExporter.class);

	public static final String NS = "";
	public static final String TAG_ROOT = "inventory";
	public static final String ATTR_COUNT = "approximateCount";
	public static final String ATTR_VERSION = "version";
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

	private final Hierarchy hier = new Hierarchy();
	private final XmlSerializer serializer = Xml.newSerializer();
	private final String xsltHref;

	public XMLExporter(String xsltHref) {
		this.xsltHref = xsltHref;
	}

	@Override public void start(OutputStream dataStream, Cursor cursor) throws IOException {
		serializer.setOutput(dataStream, ENCODING);
		serializer.startDocument(ENCODING, true);
		if (xsltHref != null) {
			// this is output as text() and that resets indent[depth] to false, need to set indent after this
			serializer.ignorableWhitespace(System.getProperty("line.separator"));
			serializer.comment(" Some browsers may not show anything if the " + xsltHref + " file cannot be found,\n"
					+ "     in that case remove the <?xml-stylesheet... ?> processing instruction. ");
			serializer.ignorableWhitespace(System.getProperty("line.separator"));
			serializer.processingInstruction("xml-stylesheet type=\"text/xsl\" href=\"" + xsltHref + "\"");
		}
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true); // must be after text()
		serializer.startTag(NS, TAG_ROOT);
		serializer.attribute(NS, ATTR_COUNT, String.valueOf(cursor.getCount()));
		serializer.attribute(NS, ATTR_VERSION, "1.0");
	}

	@Override public void processEntry(Cursor cursor) throws IOException {
		long id = DatabaseTools.getLong(cursor, CommonColumns.ID);
		long parentID = DatabaseTools.getLong(cursor, ParentColumns.PARENT_ID);
		Type type = Type.from(cursor, CommonColumns.TYPE);

		Belonging<?> belonging = hier.getOrCreate(type, id);
		belonging.name = DatabaseTools.getString(cursor, nameColumn(type));
		belonging.type = DatabaseTools.getString(cursor, "typeName");
		belonging.image = DatabaseTools.getString(cursor, BackupStreamExporter.IMAGE_NAME);
		belonging.description = DatabaseTools.getString(cursor, CommonColumns.DESCRIPTION);
		if (BuildConfig.DEBUG) {
			belonging.comment = BackupStreamExporter.buildComment(cursor);
		}

		if (type != Type.Property) { // can't have parents
			hier.put(Type.from(cursor, ParentColumns.PARENT_TYPE), parentID, belonging);
		}
	}

	@Override public void finish(Cursor cursor) throws IOException {
		hier.write(serializer);
		writeLists(serializer);
		serializer.endTag(NS, TAG_ROOT);
		serializer.endDocument();
		serializer.flush();
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

	private static String nameColumn(Type type) {
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
