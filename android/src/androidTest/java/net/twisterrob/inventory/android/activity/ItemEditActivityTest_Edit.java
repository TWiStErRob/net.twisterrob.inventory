package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import net.twisterrob.inventory.android.activity.data.ItemEditActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Item.class, Op.EditsBelonging.class})
public class ItemEditActivityTest_Edit {
	@Rule public final ActivityTestRule<ItemEditActivity> activity
			= new InventoryActivityRule<ItemEditActivity>(ItemEditActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = db.createProperty(TEST_PROPERTY);
			long parentID = db.getRoot(db.createRoom(propertyID, TEST_ROOM));
			long itemID = db.createItem(parentID, TEST_ITEM);
			db.setItemCategory(TEST_ITEM, TEST_ITEM_CATEGORY);
			getStartIntent().putExtras(Intents.bundleFromItem(itemID));
		}
	};
	@Rule public final DataBaseActor db = new AppSingletonDatabaseActor();
	private final ItemEditActivityActor itemEdit = new ItemEditActivityActor();

	@Before public void preconditions() {
		db.assertHasProperty(TEST_PROPERTY);
		db.assertHasRoomInProperty(TEST_PROPERTY, TEST_ROOM);
	}

	@Category({Op.Cancels.class})
	@Test public void testCancel() {
		itemEdit.closeToKill();
		itemEdit.assertClosing(activity.getActivity());

		db.assertHasItem(TEST_ITEM);
	}

	@Category({On.Category.class})
	@Test public void testChangeTypeWithDialog() {
		ChangeTypeDialogActor changeType = itemEdit.changeType();
		changeType.assertSelected(TEST_ITEM_CATEGORY);
		changeType.select(TEST_ITEM_CATEGORY_OTHER);

		// no changes yet
		db.assertItemHasType(TEST_ITEM, TEST_ITEM_CATEGORY);

		itemEdit.save();

		// changes applied
		db.assertItemHasType(TEST_ITEM, TEST_ITEM_CATEGORY_OTHER);
	}
}
