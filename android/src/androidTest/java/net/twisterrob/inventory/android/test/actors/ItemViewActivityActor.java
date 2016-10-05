package net.twisterrob.inventory.android.test.actors;

import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.ItemViewActivity;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class ItemViewActivityActor extends ViewActivityActor {
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
	public void hasItem(String itemName) {
		onRecyclerItem(withText(itemName)).check(matches(isCompletelyDisplayed()));
	}
}
