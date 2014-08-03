package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.fragment.ItemViewFragment.ItemEvents;

public class ItemViewActivity extends BaseDetailActivity<ItemViewFragment, ItemListFragment>
		implements
			ItemEvents,
			ItemsEvents {
	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		long parentID = getExtraParentItemID();
		setFragments(ItemViewFragment.newInstance(parentID), ItemListFragment.newInstance(parentID));
	}

	public void itemLoaded(ItemDTO item) {
		// ignore
	}

	public void newItem(long parentID) {
		startActivity(ItemEditActivity.add(parentID));
	}

	public void itemSelected(long id) {
		startActivity(ItemViewActivity.show(id));
		// TODO consider tabs as breadcrumbs?
	}

	public void itemActioned(long id) {
		startActivity(ItemEditActivity.edit(id));
	}

	@Override
	protected String checkExtras() {
		if (getExtraParentItemID() == Item.ID_ADD) {
			return "Invalid parent item ID";
		}
		return null;
	}

	private long getExtraParentItemID() {
		return getIntent().getLongExtra(Extras.PARENT_ID, Item.ID_ADD);
	}

	public static Intent show(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ItemViewActivity.class);
		intent.putExtra(Extras.PARENT_ID, itemID);
		return intent;
	}
}
