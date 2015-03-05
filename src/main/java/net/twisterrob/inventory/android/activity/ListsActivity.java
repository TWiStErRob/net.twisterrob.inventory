package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.ListListFragment;
import net.twisterrob.inventory.android.fragment.ListListFragment.ListEvents;

public class ListsActivity extends FragmentActivity implements ListEvents {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lists);
		getSupportFragmentManager().beginTransaction()
		                           .replace(R.id.activityRoot, ListListFragment.newInstance(getExtraItemID()))
		                           .commit();
	}

	private long getExtraItemID() {
		return ExtrasFactory.getItemFrom(getIntent());
	}

	@Override public void listSelected(long listID) {
		App.db().addListEntry(listID, getExtraItemID());
		App.toast("Item has been added to a list.");
		finish();
	}
	@Override public void listRemoved(long listID) {
		App.db().deleteListEntry(listID, getExtraItemID());
		App.toast("Item has been removed from a list.");
		finish();
	}

	public static Intent manage(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ListsActivity.class);
		intent.putExtra(Extras.ITEM_ID, itemID);
		return intent;
	}
}
