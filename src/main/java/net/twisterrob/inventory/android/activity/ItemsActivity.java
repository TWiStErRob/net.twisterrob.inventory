package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

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

		items = getFragment(R.id.items);
	}

	@Override
	protected void onStart() {
		super.onStart();
		items.list(getIntent().getLongExtra(Extras.PARENT_ID, Item.ID_ADD));
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
		//		ItemsFragment list = getFragment(R.id.items);
		//		if (list != null && list.isInLayout()) {
		//			list.list(id);
		//		} else {
		//			startActivity(ItemsActivity.list(id));
		//		}
	}

	public void itemActioned(long id) {
		ItemEditFragment editor = getFragment(R.id.item);
		if (editor != null && editor.isInLayout()) {
			editor.load(id);
		} else {
			startActivity(ItemEditActivity.edit(id));
		}
	}

	public static Intent list(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ItemsActivity.class);
		intent.putExtra(Extras.PARENT_ID, itemID);
		return intent;
	}
}
