package net.twisterrob.inventory.android.backup.xml;

import java.io.*;
import java.util.*;

import org.slf4j.*;
import org.xml.sax.Attributes;

import android.database.sqlite.SQLiteConstraintException;
import android.sax.*;
import android.util.Xml;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.backup.Importer;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.Types;

import static net.twisterrob.inventory.android.backup.xml.XMLExporter.*;

public class XMLImporter implements Importer {
	private static final Logger LOG = LoggerFactory.getLogger(XMLImporter.class);
	private final ImportProgressHandler progress;
	private final Database db;
	private final Types types = new Types();
	private final Map<Long, Long> itemMap = new TreeMap<>();

	public XMLImporter(ImportProgressHandler progress, Database db) {
		this.progress = progress;
		this.db = db;
	}

	public void doImport(InputStream stream) throws Exception {
		RootElement structure = getStructure();
		Xml.parse(stream, Xml.Encoding.UTF_8, structure.getContentHandler());
	}

	public RootElement getStructure() {
		RootElement root = new RootElement(TAG_ROOT);
		Element propertyElement = root.getChild(TAG_PROPERTY);
		final Element roomElement = propertyElement.getChild(TAG_ROOM);
		final Element listElement = root.getChild(TAG_LIST);
		Element listEntryElement = listElement.getChild(TAG_ITEM_REF);

		final PropertyElementListener propertyListener = new PropertyElementListener();
		propertyElement.setElementListener(propertyListener);
		propertyElement.getChild(TAG_DESCRIPTION).setEndTextElementListener(propertyListener);

		RoomElementListener roomListener = new RoomElementListener(propertyListener, new TraverseFactory() {
			private Element element = roomElement;
			private int deepestLevel = propertyListener.getLevel();
			public void onNewLevel(Parent parent) {
				if (parent.getLevel() > deepestLevel) {
					element = element.getChild(TAG_ITEM);
					ItemElementListener childListener = new ItemElementListener(parent, this);
					element.setElementListener(childListener);
					element.getChild(TAG_DESCRIPTION).setEndTextElementListener(childListener);
					deepestLevel = parent.getLevel();
				}
			}
		});
		roomElement.setElementListener(roomListener);
		roomElement.getChild(TAG_DESCRIPTION).setEndTextElementListener(roomListener);

		final ThreadLocal<Long> currentListID = new ThreadLocal<>();
		final ThreadLocal<String> currentListName = new ThreadLocal<>();
		listElement.setElementListener(new ElementListener() {
			@Override public void start(Attributes attributes) {
				String name = attributes.getValue(ATTR_NAME);
				Long id = db.findList(name);
				if (id == null) {
					id = db.createList(name);
				}
				currentListID.set(id);
				currentListName.set(name);
			}
			@Override public void end() {
				currentListID.remove();
				currentListName.remove();
			}
		});
		listEntryElement.setElementListener(new ElementListener() {
			@Override public void start(Attributes attributes) {
				long id = currentListID.get();
				long itemID = Long.parseLong(attributes.getValue(ATTR_ID));
				Long dbItemID = itemMap.get(itemID);
				if (dbItemID == null) {
					throw new IllegalArgumentException("Missing item reference to id=" + itemID);
				}
				try {
					db.addListEntry(id, dbItemID);
				} catch (SQLiteConstraintException ex) {
					progress.warning(R.string.backup_import_invalid_list_entry, currentListName.get(), id, dbItemID);
				}
			}
			@Override public void end() {

			}
		});

		root.setStartElementListener(new StartElementListener() {
			@Override public void start(Attributes attributes) {
				String count = attributes.getValue(ATTR_COUNT);
				if (count != null) {
					progress.publishStart(Long.parseLong(count));
				}
			}
		});

		return root;
	}

	private abstract class BaseElementListener implements ElementListener, EndTextElementListener, Parent {
		protected final Parent parent;
		private final TraverseFactory factory;
		private final int level;

		protected Long id;

		protected String name;
		protected String type;
		protected String image;
		protected String description;

		public BaseElementListener(Parent parent, TraverseFactory factory) {
			this.parent = parent;
			this.factory = factory;
			this.level = (parent != null? parent.getLevel() : 0) + 1;
		}

		@Override
		public void start(Attributes attributes) {
			type = attributes.getValue(ATTR_TYPE);
			name = attributes.getValue(ATTR_NAME);
			image = attributes.getValue(ATTR_IMAGE);
			factory.onNewLevel(this);
		}

		@Override public void end(String body) {
			description = body;
		}

		@Override public void end() {
			process();
			id = null;
			name = null;
			type = null;
			image = null;
			description = null;
		}

		private void process() {
			if (id != null) {
				return;
			}
			doProcess();
			progress.publishIncrement();
		}
		/**
		 * Will be called at states:<ul>
		 * <li>when there are no logical children, then at the end of the tag.</li>
		 * <li>after all the optional XML children have been parsed and stored in a member;<br>
		 *     when the first logical child is encountered that needs inserting, this may be several levels deeper.</li>
		 * </ul>
		 */
		protected abstract void doProcess();

		@Override public int getLevel() {
			return level;
		}
		@Override public Parent getParent() {
			return parent;
		}
		@Override public long getID() {
			if (id == null) {
				process();
			}
			return id;
		}
		@Override public String getName() {
			return name;
		}

		protected void loadImage(Type type) {
			if (image != null) {
				try {
					progress.importImage(type, id, name, image);
				} catch (IOException ex) {
					LOG.error("Cannot load image for {} #{} ({}): {}", type, id, name, image, ex);
				}
			}
		}
	}

	private interface Parent {
		int getLevel();
		Parent getParent();
		long getID();
		String getName();
	}

	private class PropertyElementListener extends BaseElementListener {
		public PropertyElementListener() {
			super(null, new TraverseFactory() {
				@Override public void onNewLevel(Parent parent) {
					// no op
				}
			});
		}

		@Override protected void doProcess() {
			id = db.findProperty(name);
			if (id == null) {
				Long typeID = types.getID(type);
				if (typeID == null) {
					progress.warning(R.string.backup_import_invalid_type, name, type);
					typeID = PropertyType.DEFAULT;
				}
				id = db.createProperty(typeID, name, description);
			} else {
				progress.warning(R.string.backup_import_conflict_property, name);
			}
			loadImage(Type.Property);
		}
	}

	private class RoomElementListener extends BaseElementListener {
		long rootID;

		public RoomElementListener(Parent parent, TraverseFactory factory) {
			super(parent, factory);
		}

		@Override protected void doProcess() {
			Long propertyID = parent.getID();
			id = db.findRoom(propertyID, name);
			if (id == null) {
				Long typeID = types.getID(type);
				if (typeID == null) {
					progress.warning(R.string.backup_import_invalid_type, name, type);
					typeID = RoomType.DEFAULT;
				}
				id = db.createRoom(propertyID, typeID, name, description);
			} else {
				progress.warning(R.string.backup_import_conflict_room, parent.getName(), name);
			}
			rootID = DatabaseDTOTools.getRoot(id);
			loadImage(Type.Room);
		}

		@Override public void end() {
			super.end();
			rootID = 0;
		}
		@Override public long getID() {
			super.getID();
			return rootID;
		}
	}

	public interface TraverseFactory {
		void onNewLevel(Parent parent);
	}

	private class ItemElementListener extends BaseElementListener {
		long refID;

		public ItemElementListener(Parent parent, TraverseFactory factory) {
			super(parent, factory);
		}

		@Override public void start(Attributes attributes) {
			refID = Long.parseLong(attributes.getValue(ATTR_ID));
			super.start(attributes);
		}
		@Override protected void doProcess() {
			Long parentID = parent.getID();
			id = db.findItem(parentID, name);
			if (id == null) {
				Long typeID = types.getID(type);
				if (typeID == null) {
					progress.warning(R.string.backup_import_invalid_type, name, type);
					typeID = Category.DEFAULT;
				}
				id = db.createItem(parentID, typeID, name, description);
			} else {
				Parent room = findRoom(this);
				progress.warning(R.string.backup_import_conflict_item,
						room.getParent().getName(), room.getName(), name);
			}
			itemMap.put(refID, id);
			loadImage(Type.Item);
		}
		private Parent findRoom(Parent item) {
			// try direct item in room
			Parent room = item.getParent();
			Parent property = room.getParent();
			// walk upwards if property is not a Property
			Parent top;
			while ((top = property.getParent()) != null) {
				room = property;
				property = top;
			}
			return room;
		}
	}
}
