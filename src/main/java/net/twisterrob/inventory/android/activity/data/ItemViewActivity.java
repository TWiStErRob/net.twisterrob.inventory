package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.fragment.data.ItemViewFragment.ItemEvents;

public class ItemViewActivity extends BaseDetailActivity<ItemViewFragment, ItemListFragment>
		implements ItemEvents, ItemsEvents {
	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		setIcon(R.raw.category_unknown);
		long itemID = getExtraItemID();
		setFragments(ItemViewFragment.newInstance(itemID), ItemListFragment.newInstance(itemID));
	}

	public void itemLoaded(ItemDTO item) {
		// ignore
	}

	public void itemDeleted(ItemDTO item) {
		finish();
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
		if (getExtraItemID() == Item.ID_ADD) {
			return "Invalid item ID";
		}
		return null;
	}

	private long getExtraItemID() {
		Intent intent = getIntent();
		long uri = InventoryContract.Item.getID(intent.getData());
		long extra = intent.getLongExtra(Extras.PARENT_ID, Item.ID_ADD);
		return resolve(uri, extra, Item.ID_ADD);
	}

	/** Sanity checks and uri takes precedence over extra. */
	private static long resolve(long uri, long extra, long invalid) {
		if (uri == invalid && extra == invalid) {
			return invalid;
		}
		if (uri != invalid && extra != invalid && uri != extra) {
			throw new IllegalArgumentException("Cannot get ID from both URI (" + uri + ") and extras (" + extra + ")");
		}
		if (uri != invalid) {
			return uri;
		}
		if (extra != invalid) {
			return extra;
		}
		return invalid;
	}

	public static Intent show(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ItemViewActivity.class);
		intent.putExtra(Extras.PARENT_ID, itemID);
		return intent;
	}
}
