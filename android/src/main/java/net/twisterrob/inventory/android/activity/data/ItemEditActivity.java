package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Item;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.data.ItemEditFragment;

public class ItemEditActivity extends BaseEditActivity<ItemEditFragment>
		implements ItemEditFragment.ItemEditEvents {
	@Override protected ItemEditFragment onCreateFragment() {
		return ItemEditFragment.newInstance(getExtraParentID(), getExtraItemID());
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getExtraItemID() == Item.ID_ADD) {
			setActionBarTitle(getString(R.string.item_new));
		}
	}

	@Override public void itemLoaded(ItemDTO item) {
		//setActionBarTitle(item.name); // don't set
	}

	@Override public void itemSaved(long itemID) {
		Intent data = Intents.intentFromItem(itemID);
		data.putExtra(Extras.PARENT_ID, getExtraParentID());
		// TODO check if backing out gives RESULT_CANCEL or we need to init in onCreate
		setResult(RESULT_OK, data);
		finish();
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
