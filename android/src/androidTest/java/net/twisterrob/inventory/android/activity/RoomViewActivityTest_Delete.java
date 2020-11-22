package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import net.twisterrob.android.test.espresso.DialogMatchers;
import net.twisterrob.inventory.android.activity.data.RoomViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Room.class, Op.DeletesBelonging.class})
public class RoomViewActivityTest_Delete {
	@Rule public final ActivityTestRule<RoomViewActivity> activity
			= new InventoryActivityRule<RoomViewActivity>(RoomViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = db.createProperty(TEST_PROPERTY);
			roomID = db.createRoom(propertyID, TEST_ROOM);
			getStartIntent().putExtras(Intents.bundleFromRoom(roomID));
		}
	};
	@Rule public final DataBaseActor db = new AppSingletonDatabaseActor();
	private final RoomViewActivityActor roomView = new RoomViewActivityActor();
	private long roomID;

	@Before public void preconditionsForDeletingARoom() {
		db.assertHasProperty(TEST_PROPERTY);
		db.assertHasRoom(TEST_ROOM);
	}
	@After public void closeDialog() {
		DialogMatchers.attemptCloseDialog();
	}

	@Category({Op.Cancels.class})
	@Test public void testDeleteCancel() {
		DeleteDialogActor delete = roomView.delete();
		delete.cancel();

		db.assertHasRoom(TEST_ROOM);
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testDeleteMessage() {
		DeleteDialogActor delete = roomView.delete();

		delete.checkDialogMessage(containsString(TEST_ROOM));
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testDeleteMessageWithContents() {
		db.createItemInRoom(roomID, TEST_ITEM);

		DeleteDialogActor delete = roomView.delete();

		delete.checkDialogMessage(allOf(
				containsString(TEST_ROOM),
				containsString(TEST_ITEM)
		));
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testDeleteMessageWithContentsMultiple() {
		db.createItemInRoom(roomID, TEST_ITEM);
		db.createItemInRoom(roomID, TEST_ITEM_OTHER);

		DeleteDialogActor delete = roomView.delete();

		delete.checkDialogMessage(allOf(
				containsString(TEST_ROOM),
				containsString(TEST_ITEM),
				containsString(TEST_ITEM_OTHER)
		));
	}

	@Test public void testDeleteConfirm() {
		DeleteDialogActor delete = roomView.delete();
		delete.confirm();

		roomView.assertClosing();
		db.assertHasNoRoom(TEST_ROOM);
	}

	@Test public void testDeleteConfirmWithContents() {
		db.createItemInRoom(roomID, TEST_ITEM);

		DeleteDialogActor delete = roomView.delete();
		delete.confirm();

		roomView.assertClosing();
		db.assertHasNoRoom(TEST_ROOM);
		db.assertHasNoItem(TEST_ITEM);
	}

	@Category({UseCase.Complex.class})
	@Test public void testDeleteConfirmWithContentsMultiple() {
		db.createItemInRoom(roomID, TEST_ITEM);
		db.createItemInRoom(roomID, TEST_ITEM_OTHER);

		DeleteDialogActor delete = roomView.delete();
		delete.confirm();

		roomView.assertClosing();
		db.assertHasNoRoom(TEST_ROOM);
		db.assertHasNoItem(TEST_ITEM);
		db.assertHasNoItem(TEST_ITEM_OTHER);
	}
}
