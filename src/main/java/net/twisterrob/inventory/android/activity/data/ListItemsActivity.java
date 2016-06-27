package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ListDTO;
import net.twisterrob.inventory.android.fragment.ListViewFragment.ListEvents;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class ListItemsActivity extends BaseDetailActivity<ItemListFragment> implements ItemsEvents, ListEvents {
	public ListItemsActivity() {
		super(R.plurals.list);
	}

	@Override protected ItemListFragment onCreateFragment() {
		return ItemListFragment.newListInstance(getExtraListID()).addHeader();
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
		throw new UnsupportedOperationException("Cannot create new item here");
	}

	@Override public void onContentChanged() {
		super.onContentChanged();
		setupTitleEditor();
	}

	@Override protected void updateName(String newName) {
		//noinspection WrongThread FIXME DB on UI
		App.db().updateList(getExtraListID(), newName);
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
