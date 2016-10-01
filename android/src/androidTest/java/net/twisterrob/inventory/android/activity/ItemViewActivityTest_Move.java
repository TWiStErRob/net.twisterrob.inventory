package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.activity.data.ItemViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.categories.*;
import net.twisterrob.inventory.android.test.categories.UseCase.Error;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;

@SuppressWarnings("unused")
@RunWith(AndroidJUnit4.class)
@Category({On.Item.class, Op.MovesBelonging.class})
public class ItemViewActivityTest_Move {
	@Rule public final ActivityTestRule<ItemViewActivity> activity
			= new InventoryActivityRule<ItemViewActivity>(ItemViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			propertyID = db.createProperty(TEST_PROPERTY);
			roomID = db.createRoom(propertyID, TEST_ROOM);
			itemID = db.createItemInRoom(roomID, TEST_ITEM);
			getStartIntent().putExtras(Intents.bundleFromParent(itemID));
		}
	};
	@Rule public final DataBaseActor db = new DataBaseActor();
	private final ItemViewActivityActor itemView = new ItemViewActivityActor();
	private final ItemViewActivityActor roomView = new ItemViewActivityActor();
	private long propertyID;
	private long roomID;
	private long itemID;

	@Before public void preconditions() {
		db.assertHasProperty(TEST_PROPERTY);
		db.assertHasRoomInProperty(TEST_PROPERTY, TEST_ROOM);
		db.assertHasItemInRoom(TEST_ROOM, TEST_ITEM);
	}
	@After public void closeDialog() {
		attemptCloseDialog();
	}

	@Category({Op.Cancels.class})
	@Test public void testMoveCancel() {
		db.assertHasItem(TEST_ITEM);

		MoveTargetActivityActor move = itemView.move();
		move.cancel();

		db.assertHasItem(TEST_ITEM);
	}

	@Category({Op.Cancels.class})
	@Test public void testMoveNoMove() {
		db.assertHasItem(TEST_ITEM);

		MoveTargetActivityActor move = itemView.move();
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.assertNoDialogDisplayed();
		moveDialog.assertNoToastDisplayed();

		db.assertHasItemInRoom(TEST_ROOM, TEST_ITEM);
	}

	@Category({Error.class, Op.ChecksMessage.class})
	@Test public void testMoveAlreadyExists() {
		long targetID = db.createItemInRoom(roomID, TEST_ITEM_OTHER);
		long duplicateID = db.createItem(targetID, TEST_ITEM);
		db.assertHasItemInRoom(TEST_ROOM, TEST_ITEM_OTHER);
		db.assertHasItemInItem(TEST_ITEM_OTHER, TEST_ITEM);

		onView(isRoot()).perform(waitForToastsToDisappear());
		MoveTargetActivityActor move = itemView.move();
		move.fromRoom(TEST_ROOM);
		move.selectItem(TEST_ITEM_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.assertNoDialogDisplayed();
		moveDialog.checkToastMessageDuplicate(allOf(
				containsString(TEST_ITEM),
				containsString(TEST_ITEM_OTHER)
		));

		db.assertHasItemInRoom(TEST_ROOM, TEST_ITEM);
		db.assertHasItemInItem(TEST_ITEM_OTHER, TEST_ITEM);
	}

	@Test public void testMoveToAnotherItem() {
		long targetID = db.createItemInRoom(roomID, TEST_ITEM_OTHER);
		db.assertHasItemInRoom(TEST_ROOM, TEST_ITEM_OTHER);

		MoveTargetActivityActor move = itemView.move();
		move.fromRoom(TEST_ROOM);
		move.selectItem(TEST_ITEM_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.assertNoDialogDisplayed();

		db.assertHasItemInItem(TEST_ITEM_OTHER, TEST_ITEM);
		itemView.assertShowing(TEST_ITEM_OTHER);
		itemView.hasItem(TEST_ITEM);
		itemView.assertClosing(activity.getActivity());
	}
	@Test public void testMoveToAnotherItemWithContents() {
		db.createItem(itemID, TEST_SUBITEM);
		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);

		testMoveToAnotherItem();

		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);
	}

	@Test public void testMoveToOtherRoom() {
		long targetID = db.createRoom(propertyID, TEST_ROOM_OTHER);
		db.assertHasRoomInProperty(TEST_PROPERTY, TEST_ROOM_OTHER);

		MoveTargetActivityActor move = itemView.move();
		move.fromRoom(TEST_ROOM);
		move.upToProperty(TEST_PROPERTY);
		move.selectRoom(TEST_ROOM_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.assertNoDialogDisplayed();

		db.assertHasItemInRoom(TEST_ROOM_OTHER, TEST_ITEM);
		roomView.assertShowing(TEST_ROOM_OTHER);
		roomView.hasItem(TEST_ITEM);
		itemView.assertClosing();
	}
	@Test public void testMoveToRoomWithContents() {
		db.createItem(itemID, TEST_SUBITEM);
		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);

		testMoveToOtherRoom();

		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);
	}

	@Test public void testMoveToItemInAnotherProperty() {
		long otherPropertyID = db.createProperty(TEST_PROPERTY_OTHER);
		long otherRoomID = db.createRoom(otherPropertyID, TEST_ROOM_OTHER);
		long targetID = db.createItemInRoom(otherRoomID, TEST_ITEM_OTHER);
		db.assertHasRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM_OTHER);
		db.assertHasItemInRoom(TEST_ROOM_OTHER, TEST_ITEM_OTHER);

		MoveTargetActivityActor move = itemView.move();
		move.fromRoom(TEST_ROOM);
		move.upToProperty(TEST_PROPERTY);
		move.upToProperties();
		move.selectProperty(TEST_PROPERTY_OTHER);
		move.selectRoom(TEST_ROOM_OTHER);
		move.selectItem(TEST_ITEM_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.assertNoDialogDisplayed();

		db.assertHasItemInItem(TEST_ITEM_OTHER, TEST_ITEM);
		itemView.assertShowing(TEST_ITEM_OTHER);
		itemView.hasItem(TEST_ITEM);
		itemView.assertClosing(activity.getActivity());
	}

	@Category({UseCase.Complex.class})
	@Test public void testMoveToItemInAnotherPropertyWithContents() {
		db.createItem(itemID, TEST_SUBITEM);
		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);

		testMoveToItemInAnotherProperty();

		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);
	}

	@Test public void testMoveToRoomInAnotherProperty() {
		long otherPropertyID = db.createProperty(TEST_PROPERTY_OTHER);
		long targetID = db.createRoom(otherPropertyID, TEST_ROOM_OTHER);
		db.assertHasRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM_OTHER);
		db.assertHasProperty(TEST_PROPERTY_OTHER);
		db.assertHasRoomInProperty(TEST_PROPERTY_OTHER, TEST_ROOM_OTHER);

		MoveTargetActivityActor move = itemView.move();
		move.fromRoom(TEST_ROOM);
		move.upToProperty(TEST_PROPERTY);
		move.upToProperties();
		move.selectProperty(TEST_PROPERTY_OTHER);
		move.selectRoom(TEST_ROOM_OTHER);
		MoveResultActor moveDialog = move.confirmSelection();
		moveDialog.assertNoDialogDisplayed();

		db.assertHasItemInRoom(TEST_ROOM_OTHER, TEST_ITEM);
		roomView.assertShowing(TEST_ROOM_OTHER);
		roomView.hasItem(TEST_ITEM);
		itemView.assertClosing();
	}

	@Category({UseCase.Complex.class})
	@Test public void testMoveToRoomInAnotherPropertyWithContents() {
		db.createItem(itemID, TEST_SUBITEM);
		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);

		testMoveToRoomInAnotherProperty();

		db.assertHasItemInItem(TEST_ITEM, TEST_SUBITEM);
	}
}
