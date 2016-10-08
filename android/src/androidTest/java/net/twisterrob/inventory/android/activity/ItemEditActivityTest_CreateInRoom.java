package net.twisterrob.inventory.android.activity;

import org.junit.experimental.categories.Category;

import net.twisterrob.inventory.android.activity.data.ItemEditActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.actors.ItemEditActivityActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@Category({On.Item.class, Op.CreatesBelonging.class})
public class ItemEditActivityTest_CreateInRoom extends EditActivityTest_Create<ItemEditActivity> {
	private long roomID;
	public ItemEditActivityTest_CreateInRoom() {
		super(ItemEditActivity.class, new ItemEditActivityActor(), new BelongingValues(
				TEST_ITEM, TEST_ITEM_OTHER,
				TEST_ITEM_CATEGORY, TEST_ITEM_CATEGORY_OTHER, TEST_ITEM_CATEGORY_DEFAULT) {
			@Override protected DataBaseActor.BelongingAssertions createAssertions(DataBaseActor database) {
				return database.new ItemAssertions();
			}
		});
	}

	@Override protected void createContainers() {
		roomID = database.create(TEST_PROPERTY, TEST_ROOM);
		activity.getStartIntent().putExtras(Intents.bundleFromParent(database.getRoot(roomID)));
	}

	@Override protected void createDuplicate() {
		database.createItemInRoom(roomID, TEST_ITEM);
	}
}
