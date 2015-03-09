package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ListDTO;
import net.twisterrob.inventory.android.fragment.ListViewFragment;
import net.twisterrob.inventory.android.fragment.ListViewFragment.ListEvents;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class ListItemsActivity extends BaseDetailActivity<ItemListFragment> implements ItemsEvents, ListEvents {
	@Override protected void onCreate(Bundle savedInstanceState) {
		wantDrawer = true;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected ItemListFragment onCreateFragment(Bundle savedInstanceState) {
		long listID = getExtraListID();
		ItemListFragment fragment = ItemListFragment.newListInstance(listID);
		fragment.setHeader(ListViewFragment.newInstance(listID));
		return fragment;
	}

	@Override public void listLoaded(ListDTO list) {
		setActionBarTitle(list.name);
	}

	@Override public void listDeleted(ListDTO list) {
		finish();
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

	private long getExtraListID() {
		return getIntent().getLongExtra(Extras.LIST_ID, CommonColumns.ID_ADD);
	}

	public static Intent show(long listID) {
		Intent intent = new Intent(App.getAppContext(), ListItemsActivity.class);
		intent.putExtra(Extras.LIST_ID, listID);
		return intent;
	}
}
