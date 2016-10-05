package net.twisterrob.inventory.android.content;

import java.io.InputStream;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.support.annotation.*;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.test.TestDatabaseRule;
import net.twisterrob.inventory.debug.test.R;

import static net.twisterrob.android.utils.tools.DatabaseTools.*;
import static net.twisterrob.inventory.android.content.Constants.*;
import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;
import static net.twisterrob.inventory.android.content.DatabaseMatchers.*;

public class DataBaseActor extends TestDatabaseRule {
	private final Database appDB;

	public DataBaseActor() {
		appDB = App.db();
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

	public void assertPropertyHasDescription(String propertyName, String description) {
		assertHasProperty(propertyName);
		assertThat(getOptionalString(getProperty(propertyName), Property.DESCRIPTION), is(description));
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
}
