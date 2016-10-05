package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.twisterrob.inventory.android.activity.data.ItemViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Item.class, Op.DeletesBelonging.class})
public class ItemViewActivityTest_Delete {
	@Rule public final ActivityTestRule<ItemViewActivity> activity
			= new InventoryActivityRule<ItemViewActivity>(ItemViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			itemID = db.create(TEST_PROPERTY, TEST_ROOM, TEST_ITEM);
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

	@Category({Op.Cancels.class})
	@Test public void testDeleteCancel() {
		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.cancel();

		db.assertHasItem(TEST_ITEM);
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testDeleteMessage() {
		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.checkDialogMessage(containsString(TEST_ITEM));
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testDeleteMessageWithContents() {
		db.createItem(itemID, TEST_SUBITEM);

		DeleteDialogActor deleteDialog = itemView.delete();

		deleteDialog.checkDialogMessage(allOf(
				containsString(TEST_ITEM),
				containsString(TEST_SUBITEM)
		));
	}

	@Test public void testDeleteMessageWithContentsMultiple() {
		db.createItem(itemID, TEST_SUBITEM);
		db.createItem(itemID, TEST_SUBITEM_OTHER);

		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.checkDialogMessage(allOf(
				containsString(TEST_ITEM),
				containsString(TEST_SUBITEM),
				containsString(TEST_SUBITEM_OTHER)
		));
	}

	@Test public void testDeleteConfirm() {
		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.confirm();

		db.assertHasNoItem(TEST_ITEM);
		itemView.assertClosing();
	}

	@Test public void testDeleteConfirmWithContents() {
		db.createItem(itemID, TEST_SUBITEM);

		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.confirm();

		db.assertHasNoItem(TEST_ITEM);
		db.assertHasNoItem(TEST_SUBITEM);
		itemView.assertClosing();
	}

	@Category({UseCase.Complex.class})
	@Test public void testDeleteConfirmWithContentsMultiple() {
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
