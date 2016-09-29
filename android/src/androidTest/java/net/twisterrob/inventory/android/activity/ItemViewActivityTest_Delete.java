package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.data.ItemViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
public class ItemViewActivityTest_Delete {
	@Rule public final ActivityTestRule<ItemViewActivity> activity
			= new InventoryActivityRule<ItemViewActivity>(ItemViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = App.db().createProperty(PropertyType.DEFAULT, TEST_PROPERTY, NO_DESCRIPTION);
			long roomID = App.db().createRoom(propertyID, RoomType.DEFAULT, TEST_ROOM, NO_DESCRIPTION);
			itemID = db.createItemInRoom(roomID, TEST_ITEM);
			getStartIntent().putExtras(Intents.bundleFromParent(itemID));
		}
	};
	@Rule public final DataBaseActor db = new DataBaseActor();
	private final ItemViewActivityActor itemView = new ItemViewActivityActor();
	private long itemID;

	@Before public void preconditions() {
		db.assertHasProperty(TEST_PROPERTY);
		db.assertHasRoom(TEST_ROOM);
		db.assertHasItem(TEST_ITEM);
	}
	@After public void closeDialog() {
		attemptCloseDialog();
	}

	@Test public void testDeleteCancel() throws IOException {
		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.cancel();

		db.assertHasItem(TEST_ITEM);
	}

	@Test public void testDeleteMessage() throws IOException {
		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.checkDialogMessage(containsString(TEST_ITEM));
	}
	@Test public void testDeleteMessageWithContents() throws IOException {
		db.createItem(itemID, TEST_SUBITEM);

		DeleteDialogActor deleteDialog = itemView.delete();

		deleteDialog.checkDialogMessage(allOf(
				containsString(TEST_ITEM),
				containsString(TEST_SUBITEM)
		));
	}
	@Test public void testDeleteMessageWithContentsMultiple() throws IOException {
		db.createItem(itemID, TEST_SUBITEM);
		db.createItem(itemID, TEST_SUBITEM_OTHER);

		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.checkDialogMessage(allOf(
				containsString(TEST_ITEM),
				containsString(TEST_SUBITEM),
				containsString(TEST_SUBITEM_OTHER)
		));
	}

	@Test public void testDeleteConfirm() throws IOException {
		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.confirm();

		db.assertHasNoItem(TEST_ITEM);
		itemView.assertClosing();
	}
	@Test public void testDeleteConfirmWithContents() throws IOException {
		db.createItem(itemID, TEST_SUBITEM);

		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.confirm();

		db.assertHasNoItem(TEST_ITEM);
		db.assertHasNoItem(TEST_SUBITEM);
		itemView.assertClosing();
	}
	@Test public void testDeleteConfirmWithContentsMultiple() throws IOException {
		db.createItem(itemID, TEST_SUBITEM);
		db.createItem(itemID, TEST_SUBITEM_OTHER);

		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.confirm();

		db.assertHasNoItem(TEST_ITEM);
		db.assertHasNoItem(TEST_SUBITEM);
		db.assertHasNoItem(TEST_SUBITEM_OTHER);
		itemView.assertClosing();
	}
}
