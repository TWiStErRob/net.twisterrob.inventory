package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.ItemEditFragment;

public class ItemEditActivity extends BaseEditActivity<ItemEditFragment> {
	@Override
	protected ItemEditFragment onCreateFragment(Bundle savedInstanceState) {
		return ItemEditFragment.newInstance(getExtraParentID(), getExtraItemID());
	}

	private long getExtraItemID() {
		return getIntent().getLongExtra(Extras.ITEM_ID, Item.ID_ADD);
	}

	private long getExtraParentID() {
		return getIntent().getLongExtra(Extras.PARENT_ID, Item.ID_ADD);
	}

	public static Intent add(long parentID) {
		Intent intent = new Intent(App.getAppContext(), ItemEditActivity.class);
		intent.putExtra(Extras.PARENT_ID, parentID);
		return intent;
	}

	public static Intent edit(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ItemEditActivity.class);
		intent.putExtra(Extras.ITEM_ID, itemID);
		return intent;
	}
}
