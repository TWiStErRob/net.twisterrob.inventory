package net.twisterrob.inventory.android.content;

import java.io.InputStream;

import org.hamcrest.Matcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.support.annotation.*;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.database.test_helpers.R;
import net.twisterrob.inventory.android.test.TestDatabaseRule;
import net.twisterrob.java.io.IOTools;

import static net.twisterrob.android.utils.tools.DatabaseTools.*;
import static net.twisterrob.inventory.android.content.Constants.*;
import static net.twisterrob.inventory.android.content.DatabaseMatchers.*;

@VisibleForTesting
@SuppressWarnings({"unused", "TryFinallyCanBeTryWithResources"})
public class DataBaseActor extends TestDatabaseRule {
	private final Database appDB;

	public DataBaseActor(Database appDB) {
		super();
		this.appDB = appDB;
	}

	public Database getDatabase() {
		return appDB;
	}

	public void assertHasNoUserData() {
		assertHasNoProperties();
		assertHasNoRooms();
		assertHasNoItems();
		assertHasNoLists();
		assertHasNoListEntries();
	}

	public void assertHasNoProperties() {
		assertThat(DatabaseTools.singleLong(appDB.stats(), "properties"), is(0L));
	}
	public void assertHasNoRooms() {
		assertThat(DatabaseTools.singleLong(appDB.stats(), "rooms"), is(0L));
	}
	public void assertHasNoItems() {
		assertThat(DatabaseTools.singleLong(appDB.stats(), "items"), is(0L));
	}
	public void assertHasNoLists() {
		assertThat(DatabaseTools.singleLong(appDB.stats(), "lists"), is(0L));
	}
	public void assertHasNoListEntries() {
		assertThat(DatabaseTools.singleLong(appDB.stats(), "list_entries"), is(0L));
	}

	public void assertHasProperty(String propertyName) {
		assertThat(testDB, hasInvProperty(propertyName));
	}
	public void assertHasNoProperty(String propertyName) {
		assertThat(testDB, not(hasInvProperty(propertyName)));
	}
	public void assertHasRoom(String roomName) {
		assertThat(testDB, hasInvRoom(roomName));
	}
	public void assertHasRoomInProperty(String propertyName, String roomName) {
		assertThat(testDB, allOf(
				hasInvProperty(propertyName),
				hasInvRoom(roomName),
				hasInvRoomInProperty(propertyName, roomName)
		));
	}
	public void assertHasNoRoom(String roomName) {
		assertThat(testDB, not(hasInvRoom(roomName)));
	}
	public void assertHasNoRoomInProperty(String propertyName, String roomName) {
		assertThat(testDB, not(hasInvRoomInProperty(propertyName, roomName)));
	}
	public void assertHasItem(String itemName) {
		assertThat(testDB, hasInvItem(itemName));
	}
	public void assertHasItemInItem(String parentName, String childName) {
		assertThat(testDB, allOf(hasInvItem(childName), hasInvItemIn(parentName, childName)));
	}
	public void assertHasItemInRoom(String roomName, String itemName) {
		assertThat(testDB, allOf(hasInvItem(itemName), hasInvItemInRoom(roomName, itemName)));
	}
	public void assertHasNoItem(String itemName) {
		assertThat(testDB, not(hasInvItem(itemName)));
	}
	public void assertHasList(String listName) {
		assertThat(testDB, hasInvList(listName));
	}

	public void assertPropertyHasDescription(String propertyName, String description) {
		assertHasProperty(propertyName);
		assertThat(getOptionalString(getProperty(propertyName), Property.DESCRIPTION), is(description));
	}
	public void assertRoomHasDescription(String roomName, String description) {
		assertHasRoom(roomName);
		assertThat(getOptionalString(getRoom(roomName), Room.DESCRIPTION), is(description));
	}
	public void assertItemHasDescription(String itemName, String description) {
		assertHasItem(itemName);
		assertThat(getOptionalString(getItem(itemName), Item.DESCRIPTION), is(description));
	}

	public void assertPropertyHasType(String propertyName, @StringRes int expectedType) {
		assertPropertyHasType(propertyName, getTargetContext().getResources().getResourceEntryName(expectedType));
	}
	public void assertPropertyHasType(String propertyName, String expectedType) {
		Long expectedTypeID = testDB.getID(R.string.query_property_type_by_name, expectedType);
		assertPropertyHasType(propertyName, expectedTypeID);
	}
	public void assertPropertyHasType(String propertyName, long expectedTypeID) {
		assertHasProperty(propertyName);
		assertThat(getOptionalLong(getProperty(propertyName), Property.TYPE_ID), is(expectedTypeID));
	}

	public void assertRoomHasType(String roomName, @StringRes int expectedType) {
		assertRoomHasType(roomName, getTargetContext().getResources().getResourceEntryName(expectedType));
	}
	public void assertRoomHasType(String roomName, String expectedType) {
		Long expectedTypeID = testDB.getID(R.string.query_room_type_by_name, expectedType);
		assertRoomHasType(roomName, expectedTypeID);
	}
	public void assertRoomHasType(String roomName, long expectedTypeID) {
		assertHasRoom(roomName);
		assertThat(getOptionalLong(getRoom(roomName), Room.TYPE_ID), is(expectedTypeID));
	}

	public void assertItemHasType(String itemName, @StringRes int expectedType) {
		assertItemHasType(itemName, getTargetContext().getResources().getResourceEntryName(expectedType));
	}
	public void assertItemHasType(String itemName, String expectedType) {
		Long expectedTypeID = testDB.getID(R.string.query_category_by_name, expectedType);
		assertItemHasType(itemName, expectedTypeID);
	}
	public void assertItemHasType(String itemName, long expectedTypeID) {
		assertHasItem(itemName);
		assertThat(getOptionalLong(getItem(itemName), Item.TYPE_ID), is(expectedTypeID));
	}

	public void assertPropertyHasImage(String propertyName) {
		assertThat(getOptionalBoolean(getProperty(propertyName), Property.HAS_IMAGE), is(true));
	}
	public void assertPropertyHasImage(String propertyName, @ColorInt int backgroundColor) {
		Cursor property = getProperty(propertyName);
		assertThat(getOptionalBoolean(property, Property.HAS_IMAGE), is(true));
		@SuppressWarnings("ConstantConditions")
		long id = DatabaseTools.singleLong(property, Property.ID);
		assertImageBackground(InventoryContract.Property.imageUri(id), backgroundColor);
	}
	public void assertRoomHasImage(String roomName) {
		assertThat(getOptionalBoolean(getRoom(roomName), Room.HAS_IMAGE), is(true));
	}
	public void assertRoomHasImage(String roomName, @ColorInt int backgroundColor) {
		Cursor room = getRoom(roomName);
		assertThat(getOptionalBoolean(room, Room.HAS_IMAGE), is(true));
		@SuppressWarnings("ConstantConditions")
		long id = DatabaseTools.singleLong(room, Room.ID);
		assertImageBackground(InventoryContract.Room.imageUri(id), backgroundColor);
	}
	public void assertItemHasImage(String itemName) {
		assertThat(getOptionalBoolean(getItem(itemName), Item.HAS_IMAGE), is(true));
	}
	public void assertItemHasImage(String itemName, @ColorInt int backgroundColor) {
		Cursor item = getItem(itemName);
		assertThat(getOptionalBoolean(item, Item.HAS_IMAGE), is(true));
		@SuppressWarnings("ConstantConditions")
		long id = DatabaseTools.singleLong(item, Item.ID);
		assertImageBackground(InventoryContract.Item.imageUri(id), backgroundColor);
	}

	private void assertImageBackground(Uri image, @ColorInt int backgroundColor) {
		InputStream stream = null;
		try {
			stream = getTargetContext().getContentResolver().openInputStream(image);
			Bitmap bitmap = BitmapFactory.decodeStream(stream);
			try {
				assertThat(bitmap.getPixel(0, 0), is(backgroundColor));
			} finally {
				bitmap.recycle();
			}
		} catch (Exception ex) {
			throw new AssertionError(ex);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}

	public long getRoot(long roomID) {
		return appDB.getRoomRoot(roomID);
	}

	private Cursor getProperty(String propertyName) {
		Long id = testDB.getID(R.string.query_property_by_name, propertyName);
		assertNotNull("Property not found: " + propertyName, id);
		Cursor cursor = appDB.getProperty(id);
		try {
			return ensureFirst(DatabaseTools.clone(cursor));
		} finally {
			cursor.close();
		}
	}
	public long createProperty(String propertyName) {
		long propertyID = appDB.createProperty(PropertyType.DEFAULT, propertyName, NO_DESCRIPTION);
		assertHasProperty(propertyName);
		return propertyID;
	}
	private Cursor getRoom(String roomName) {
		Long id = testDB.getID(R.string.query_room_by_name, roomName);
		assertNotNull("Room not found: " + roomName, id);
		Cursor cursor = appDB.getRoom(id);
		try {
			return ensureFirst(DatabaseTools.clone(cursor));
		} finally {
			cursor.close();
		}
	}
	public long createRoom(String propertyName, String roomName) {
		long propertyID = appDB.findProperty(propertyName);
		long roomID = createRoom(propertyID, roomName);
		assertHasRoomInProperty(propertyName, roomName);
		return roomID;
	}
	public long createRoom(long propertyID, String roomName) {
		long roomID = appDB.createRoom(propertyID, RoomType.DEFAULT, roomName, NO_DESCRIPTION);
		assertHasRoom(roomName);
		return roomID;
	}
	public long createItemInRoom(long roomID, String itemName) {
		long roomRootID = getRoot(roomID);
		long itemID = appDB.createItem(roomRootID, Category.DEFAULT, itemName, NO_DESCRIPTION);
		assertHasItem(itemName);
		return itemID;
	}
	private Cursor getItem(String itemName) {
		Long id = testDB.getID(R.string.query_item_by_name, itemName);
		assertNotNull("Item not found: " + itemName, id);
		Cursor cursor = appDB.getItem(id, false);
		try {
			return ensureFirst(DatabaseTools.clone(cursor));
		} finally {
			cursor.close();
		}
	}
	public long createItem(long parentID, String itemName) {
		long itemID = appDB.createItem(parentID, Category.DEFAULT, itemName, NO_DESCRIPTION);
		assertHasItem(itemName);
		return itemID;
	}
	public long create(String property, String room) {
		long propertyID = createProperty(property);
		return createRoom(propertyID, room);
	}
	public long create(String property, String room, String item) {
		return create(property, room, new String[] {item})[0];
	}
	public long[] create(String property, String room, String... items) {
		long propertyID = createProperty(property);
		long roomID = createRoom(propertyID, room);
		long[] itemIDs = new long[items.length];
		for (int i = 0; i < items.length; i++) {
			itemIDs[i] = createItemInRoom(roomID, items[i]);
		}
		return itemIDs;
	}

	public void assertImageCount(Matcher<Long> countMatcher) {
		assertThat(testDB, countImages(countMatcher));
	}
	public void setItemCategory(String itemName, @StringRes int category) {
		Cursor item = getItem(itemName);
		appDB.updateItem(
				DatabaseTools.getLong(item, Item.ID),
				testDB.getID(R.string.query_category_by_name,
						getTargetContext().getResources().getResourceEntryName(category)),
				DatabaseTools.getString(item, Item.NAME),
				DatabaseTools.getString(item, Item.DESCRIPTION)
		);
		assertItemHasType(itemName, category);
	}

	public interface BelongingAssertions {
		void assertHasNoBelongingOfType();
		void assertHasBelonging(String name);
		void assertHasNoBelonging(String name);
		void assertHasDescription(String name, String description);
		void assertHasType(String name, @StringRes int type);
		void assertHasImage(String name, @ColorInt int color);
	}

	public class PropertyAssertions implements BelongingAssertions {
		@Override public void assertHasNoBelongingOfType() {
			assertHasNoProperties();
		}
		@Override public void assertHasBelonging(String name) {
			assertHasProperty(name);
		}
		@Override public void assertHasNoBelonging(String name) {
			assertHasNoProperty(name);
		}
		@Override public void assertHasType(String name, @StringRes int type) {
			assertPropertyHasType(name, type);
		}
		@Override public void assertHasImage(String name, @ColorInt int color) {
			assertPropertyHasImage(name, color);
		}
		@Override public void assertHasDescription(String name, String description) {
			assertPropertyHasDescription(name, description);
		}
	}

	public class RoomAssertions implements BelongingAssertions {
		@Override public void assertHasNoBelongingOfType() {
			assertHasNoRooms();
		}
		@Override public void assertHasBelonging(String name) {
			assertHasRoom(name);
		}
		@Override public void assertHasNoBelonging(String name) {
			assertHasNoRoom(name);
		}
		@Override public void assertHasType(String name, @StringRes int type) {
			assertRoomHasType(name, type);
		}
		@Override public void assertHasImage(String name, @ColorInt int color) {
			assertRoomHasImage(name, color);
		}
		@Override public void assertHasDescription(String name, String description) {
			assertRoomHasDescription(name, description);
		}
	}

	public class ItemAssertions implements BelongingAssertions {
		@Override public void assertHasNoBelongingOfType() {
			assertHasNoItems();
		}
		@Override public void assertHasBelonging(String name) {
			assertHasItem(name);
		}
		@Override public void assertHasNoBelonging(String name) {
			assertHasNoItem(name);
		}
		@Override public void assertHasType(String name, @StringRes int type) {
			assertItemHasType(name, type);
		}
		@Override public void assertHasImage(String name, @ColorInt int color) {
			assertItemHasImage(name, color);
		}
		@Override public void assertHasDescription(String name, String description) {
			assertItemHasDescription(name, description);
		}
	}
}
