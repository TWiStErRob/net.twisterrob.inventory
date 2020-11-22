package net.twisterrob.inventory.android.backup.xml;

import java.io.*;
import java.util.*;

import org.slf4j.*;
import org.xml.sax.Attributes;

import android.content.res.Resources;
import android.database.sqlite.SQLiteConstraintException;
import android.sax.*;
import android.util.Xml;

import androidx.annotation.*;

import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.Types;
import net.twisterrob.java.utils.ObjectTools;

import static net.twisterrob.inventory.android.backup.xml.XMLExporter.*;

public class XMLImporter implements Importer {
	private static final Logger LOG = LoggerFactory.getLogger(XMLImporter.class);
	private ImportProgress progress;
	private ImportImageGetter images;
	private final Resources res;
	private final Database db;
	private final Types types;
	private final Map<Long, Long> itemMap = new TreeMap<>();

	public XMLImporter(Resources res, Database db, Types types) {
		this.res = ObjectTools.checkNotNull(res);
		this.db = ObjectTools.checkNotNull(db);
		this.types = ObjectTools.checkNotNull(types);
	}

	public void doImport(@NonNull InputStream stream, @Nullable ImportProgress progress,
			@Nullable ImportImageGetter getter) throws Exception {
		this.progress = progress == null? DUMMY_HANDLER : progress;
		this.images = getter == null? DUMMY_GETTER : getter;
		RootElement structure = getStructure();
		Xml.parse(stream, Xml.Encoding.UTF_8, structure.getContentHandler());
	}

	private RootElement getStructure() {
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
				//noinspection ConstantConditions Entries are only processed inside a list.
				long listID = currentListID.get();
				long itemID = Long.parseLong(attributes.getValue(ATTR_ID));
				Long dbItemID = itemMap.get(itemID);
				if (dbItemID == null) {
					throw new IllegalArgumentException("Missing item reference to id=" + itemID);
				}
				try {
					db.addListEntry(listID, dbItemID);
				} catch (SQLiteConstraintException ex) {
					progress.warning(res.getString(R.string.backup_import_invalid_list_entry,
							currentListName.get(), itemID));
				}
			}
			@Override public void end() {

			}
		});

		root.setStartElementListener(new StartElementListener() {
			@Override public void start(Attributes attributes) {
				String count = attributes.getValue(ATTR_COUNT);
				if (count != null) {
					progress.publishStart(Integer.parseInt(count));
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
					images.importImage(type, id, name, image);
				} catch (IOException ex) {
					// CONSIDER throwing to stop import?
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
					progress.warning(res.getString(R.string.backup_import_invalid_type, name, type));
					typeID = PropertyType.DEFAULT;
				}
				id = db.createProperty(typeID, name, description);
				loadImage(Type.Property);
			} else {
				progress.warning(res.getString(R.string.backup_import_conflict_property, name));
			}
		}
	}

	private class RoomElementListener extends BaseElementListener {
		long rootID;

		public RoomElementListener(Parent parent, TraverseFactory factory) {
			super(parent, factory);
		}

		@Override protected void doProcess() {
			long propertyID = parent.getID();
			id = db.findRoom(propertyID, name);
			boolean created;
			if (id == null) {
				Long typeID = types.getID(type);
				if (typeID == null) {
					progress.warning(res.getString(R.string.backup_import_invalid_type, name, type));
					typeID = RoomType.DEFAULT;
				}
				id = db.createRoom(propertyID, typeID, name, description);
				created = true;
				loadImage(Type.Room);
			} else {
				progress.warning(res.getString(R.string.backup_import_conflict_room, parent.getName(), name));
				created = false;
			}
			rootID = db.getRoomRoot(id);
			if (rootID == Item.ID_ADD) {
				throw new IllegalStateException(
						"Cannot find root of" + (created? " just created " : " ") + "room " + id + "(" + name + ")");
			}
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
			long parentID = parent.getID();
			id = db.findItem(parentID, name);
			if (id == null) {
				Long typeID = types.getID(type);
				if (typeID == null) {
					progress.warning(res.getString(R.string.backup_import_invalid_type, name, type));
					typeID = Category.DEFAULT;
				}
				id = db.createItem(parentID, typeID, name, description);
				loadImage(Type.Item);
			} else {
				Parent room = findRoom(this);
				progress.warning(res.getString(R.string.backup_import_conflict_item,
						room.getParent().getName(), room.getName(), name));
			}
			itemMap.put(refID, id);
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

	private static final ImportProgress DUMMY_HANDLER = new ImportProgress() {
		@Override public void publishStart(int size) {
			// NO OP
		}
		@Override public void publishIncrement() {
			// NO OP
		}
		@Override public void warning(String message) {
			// NO OP
		}
		@Override public void error(String message) {
			// NO OP
		}
	};

	private static final ImportImageGetter DUMMY_GETTER = new ImportImageGetter() {
		@Override public void importImage(Type type, long id, String name, String image) {
			// NO OP
		}
	};
}
