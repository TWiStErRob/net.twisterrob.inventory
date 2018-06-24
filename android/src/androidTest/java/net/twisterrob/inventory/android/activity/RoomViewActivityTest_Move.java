package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.twisterrob.android.test.espresso.DialogMatchers;
import net.twisterrob.inventory.android.activity.data.RoomViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.categories.*;
import net.twisterrob.inventory.android.test.categories.UseCase.Error;

import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Room.class, Op.MovesBelonging.class})
public class RoomViewActivityTest_Move {
	@Rule public final ActivityTestRule<RoomViewActivity> activity
			= new InventoryActivityRule<RoomViewActivity>(RoomViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = db.createProperty(TEST_PROPERTY);
			roomID = db.createRoom(propertyID, TEST_ROOM);
			getStartIntent().putExtras(Intents.bundleFromRoom(roomID));
			db.createProperty(TEST_PROPERTY_OTHER);
		}
	};
	@Rule public final DataBaseActor db = new AppSingletonDatabaseActor();
	private final RoomViewActivityActor roomView = new RoomViewActivityActor();
	private final PropertyViewActivityActor propertyView = new PropertyViewActivityActor();
	private long roomID;

	@Before public void preconditionsForMovingRoomBetweenProperties() {
		db.assertHasProperty(TEST_PROPERTY);
		db.assertHasRoomInProperty(TEST_PROPERTY, TEST_ROOM);

		db.assertHasProperty(TEST_PROPERTY_OTHER);
		db.assertHasNoRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM);
	}
	@After public void closeDialog() {
		DialogMatchers.attemptCloseDialog();
	}

	@Category({Op.Cancels.class})
	@Test public void testMoveCancel() {
		db.assertHasRoom(TEST_ROOM);

		MoveTargetActivityActor move = roomView.move();
		move.cancel();

		db.assertHasRoom(TEST_ROOM);
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testMoveConfirmMessage() {
		MoveTargetActivityActor move = roomView.move();
		move.selectProperty(TEST_PROPERTY_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();

		moveDialog.checkDialogMessage(allOf(containsString(TEST_ROOM), containsString(TEST_PROPERTY)));
	}

	@Category({Op.Cancels.class})
	@Test public void testMoveConfirmCancel() {
		MoveTargetActivityActor move = roomView.move();
		move.selectProperty(TEST_PROPERTY_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.cancel();

		db.assertHasRoomInProperty(TEST_PROPERTY, TEST_ROOM);
		roomView.assertShowing(TEST_ROOM);
	}

	@Test public void testMove() {
		MoveTargetActivityActor move = roomView.move();
		move.selectProperty(TEST_PROPERTY_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.confirm();

		db.assertHasRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM);
		propertyView.assertShowing(TEST_PROPERTY_OTHER);
		roomView.assertClosing();
		propertyView.hasRoom(TEST_ROOM);
	}

	@Category({UseCase.Complex.class})
	@Test public void testMoveWithContents() {
		long itemID = db.createItemInRoom(roomID, TEST_ITEM);
		db.createItem(itemID, TEST_SUBITEM);
		db.assertHasItemInRoom(TEST_ROOM, TEST_ITEM);
		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);

		MoveTargetActivityActor move = roomView.move();
		move.selectProperty(TEST_PROPERTY_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.confirm();

		db.assertHasRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM);
		db.assertHasItemInRoom(TEST_ROOM, TEST_ITEM);
		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);
		propertyView.assertShowing(TEST_PROPERTY_OTHER);
		roomView.assertClosing();
		propertyView.hasRoom(TEST_ROOM);
	}

	@Category({Error.class, Op.ChecksMessage.class})
	@Test public void testMoveAlreadyExists() {
		@SuppressWarnings("unused")
		long duplicateID = db.createRoom(TEST_PROPERTY_OTHER, TEST_ROOM);

		MoveTargetActivityActor move = roomView.move();
		move.selectProperty(TEST_PROPERTY_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.confirm();

		db.assertHasRoom(TEST_ROOM);
		db.assertHasRoomInProperty(TEST_PROPERTY, TEST_ROOM);
		db.assertHasRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM);

		moveDialog.checkToastMessageDuplicate(allOf(
				containsString(TEST_ROOM),
				containsString(TEST_PROPERTY_OTHER)
		));
	}
}
