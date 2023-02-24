package net.twisterrob.inventory.android.activity;

import org.junit.experimental.categories.Category;

import net.twisterrob.inventory.android.activity.data.ItemEditActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.actors.ItemEditActivityActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@Category({On.Item.class, Op.CreatesBelonging.class})
public class ItemEditActivityTest_CreateInItem extends EditActivityTest_Create<ItemEditActivity> {
	private long itemID;
	public ItemEditActivityTest_CreateInItem() {
		super(ItemEditActivity.class, new ItemEditActivityActor(), new BelongingValues(
				TEST_SUBITEM, TEST_SUBITEM_OTHER,
				TEST_ITEM_CATEGORY, TEST_ITEM_CATEGORY_OTHER, TEST_ITEM_CATEGORY_DEFAULT) {
			@Override protected DataBaseActor.BelongingAssertions createAssertions(DataBaseActor database) {
				return database.new ItemAssertions();
			}
		});
	}

	@Override public void preconditions() {
		// hasNoItems() won't work, because the container is an item
		database.assertHasNoItem(TEST_SUBITEM);
	}
	@Override protected void createContainers() {
		itemID = database.create(TEST_PROPERTY, TEST_ROOM, TEST_ITEM);
		activity.getStartIntent().putExtras(Intents.bundleFromParent(itemID));
	}

	@Override protected void createDuplicate() {
		database.createItem(itemID, TEST_SUBITEM);
	}
}
