package net.twisterrob.inventory.android.test.actors;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;

public class CategoryActivityActor extends ItemContainingViewActivityActor {
	public CategoryActivityActor() {
		super(CategoryActivity.class);
	}

	@Override public void assertShowing(String itemName) {
		assertActionTitle(itemName);
	}

	public CategoryActivityActor flatten() {
		clickActionBar(R.id.action_category_viewAllItems);
		CategoryActivityActor activity = new CategoryActivityActor();
		activity.assertIsInFront();
		return activity;
	}
}
