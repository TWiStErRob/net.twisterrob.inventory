package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.fragment.ItemViewFragment.ItemEvents;

public class ItemViewActivity extends BaseListActivity implements ItemEvents, ItemsEvents {
	private ItemViewFragment item;
	private ItemListFragment items;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.item_view_activity);

		long currentParentID = getExtraParentItemID();
		if (currentParentID == Item.ID_ADD) {
			Toast.makeText(this, "Invalid parent item ID", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		item = ItemViewFragment.newInstance(currentParentID);
		items = ItemListFragment.newInstance(currentParentID);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.item, item);
		ft.replace(R.id.items, items);
		ft.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		item.refresh();
		items.refresh();
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

	private long getExtraParentItemID() {
		return getIntent().getLongExtra(Extras.PARENT_ID, Item.ID_ADD);
	}

	public static Intent show(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ItemViewActivity.class);
		intent.putExtra(Extras.PARENT_ID, itemID);
		return intent;
	}
}
