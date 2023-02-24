package net.twisterrob.inventory.android.backup.xml;

import java.io.*;
import java.util.*;

import javax.xml.XMLConstants;

import org.slf4j.*;
import org.xmlpull.v1.XmlSerializer;

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Xml;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.backup.BuildConfig;
import net.twisterrob.inventory.android.backup.exporters.BackupStreamExporter;
import net.twisterrob.inventory.android.content.Database;
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
	private static final String NS_XSI = "xsi";
	private static final String NS_XSI_URI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
	private static final String XS_NO_LOCATION = "noNamespaceSchemaLocation";

	private final Hierarchy hier = new Hierarchy();
	private final XmlSerializer serializer = Xml.newSerializer();
	private final String xsltHref;
	private final String xsdHref;
	private final Database db;

	public XMLExporter(String xsltHref, String xsdHref, Database db) {
		this.xsltHref = xsltHref;
		this.xsdHref = xsdHref;
		this.db = db;
	}

	@Override public void start(OutputStream dataStream, Cursor cursor) throws IOException {
		serializer.setOutput(dataStream, ENCODING);
		serializer.startDocument(ENCODING, true);
		if (xsltHref != null) {
			serializer.ignorableWhitespace(System.getProperty("line.separator"));
			escapedComment(serializer, " Some browsers may not show anything if the " + xsltHref + " file cannot be found,\n"
					+ "     in that case remove the <?xml-stylesheet... ?> processing instruction. ");
			serializer.ignorableWhitespace(System.getProperty("line.separator"));
			serializer.processingInstruction("xml-stylesheet type=\"text/xsl\" href=\"" + xsltHref + "\"");
		}
		if (xsdHref != null) {
			serializer.ignorableWhitespace(System.getProperty("line.separator"));
			escapedComment(serializer, " An up to date version of the XSD Schema can be found at\n"
					+ "     https://github.com/TWiStErRob/net.twisterrob.inventory ");
			serializer.setPrefix(NS_XSI, NS_XSI_URI);
		}
		// Need to set indent right before root tag, because ignorableWhitespace
		// is output as text() and that resets indent[depth] to false.
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
		serializer.startTag(NS, TAG_ROOT);
		serializer.attribute(NS, ATTR_COUNT, String.valueOf(cursor.getCount()));
		serializer.attribute(NS, ATTR_VERSION, "1.0");
		if (xsdHref != null) {
			serializer.attribute(NS_XSI_URI, XS_NO_LOCATION, xsdHref);
		}
	}

	@Override public void processEntry(Cursor cursor) {
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
		Cursor lists = db.listLists(Item.ID_ADD);
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
		Cursor item = db.listItemsInList(listID);
		try {
			while (item.moveToNext()) {
				output.startTag(NS, TAG_ITEM_REF);
				long itemID = DatabaseTools.getLong(item, CommonColumns.ID);
				String itemName = DatabaseTools.getString(item, CommonColumns.NAME);
				output.attribute(NS, ATTR_ID, String.valueOf(itemID));
				output.endTag(NS, TAG_ITEM_REF);
				escapedComment(output, itemName);
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

	private static void escapedComment(XmlSerializer output, String comment) throws IOException {
		output.comment(comment.replace("--", "&#x002d;&#x002d;"));
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
				escapedComment(output, comment);
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
				escapedComment(output, comment);
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
