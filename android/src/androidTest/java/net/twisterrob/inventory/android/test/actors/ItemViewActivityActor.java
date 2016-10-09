package net.twisterrob.inventory.android.test.actors;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.ItemViewActivity;

public class ItemViewActivityActor extends ItemContainingViewActivityActor {
	public ItemViewActivityActor() {
		super(ItemViewActivity.class);
	}
	@Override public void assertShowing(String itemName) {
		assertActionTitle(itemName);
	}

	public DeleteDialogActor delete() {
		clickActionOverflow(R.string.item_delete);
		return new DeleteDialogActor();
	}
	public MoveTargetActivityActor move() {
		clickActionBar(R.id.action_item_move);
		return new MoveTargetActivityActor();
	}
	public ItemEditActivityActor edit() {
		clickActionBar(R.id.action_item_edit);
		return new ItemEditActivityActor();
	}
}
