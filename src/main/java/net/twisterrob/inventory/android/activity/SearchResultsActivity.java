package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.activity.data.BaseDetailActivity.NoFragment;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class SearchResultsActivity extends BaseDetailActivity<NoFragment, ItemListFragment> implements ItemsEvents {
	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		hideDetails();
		setFragments(null, ItemListFragment.newSearchInstance(""));
	}

	public void itemSelected(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}

	public void itemActioned(long itemID) {
		startActivity(ItemEditActivity.edit(itemID));
	}

	public void newItem(long parentID) {
		// ignore
	}

	public static Intent show() {
		Intent intent = new Intent(App.getAppContext(), SearchResultsActivity.class);
		return intent;
	}
}
