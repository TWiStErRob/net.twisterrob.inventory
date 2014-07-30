package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.ItemsFragment.ItemEvents;

public class ItemsActivity extends BaseListActivity implements ItemEvents {
	private ItemsFragment items;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.items);

		long currentParentID = getExtraParentItemID();
		if (currentParentID == Item.ID_ADD) {
			Toast.makeText(this, "Invalid parent item ID", Toast.LENGTH_LONG).show();
			finish();
		}

		items = ItemsFragment.newInstance(currentParentID);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.items, items);
		ft.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		items.refresh();
	}

	public void newItem() {
		startActivity(ItemEditActivity.add());
	}

	public void itemSelected(long id) {
		startActivity(ItemsActivity.list(id));
		// TODO consider tabs as breadcrumbs?
	}

	public void itemActioned(long id) {
		startActivity(ItemEditActivity.edit(id));
	}

	private long getExtraParentItemID() {
		return getIntent().getLongExtra(Extras.PARENT_ID, Item.ID_ADD);
	}

	public static Intent list(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ItemsActivity.class);
		intent.putExtra(Extras.PARENT_ID, itemID);
		return intent;
	}
}
