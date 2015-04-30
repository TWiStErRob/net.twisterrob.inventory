package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.ListListFragment.ListsEvents;

public class ListsActivity extends FragmentActivity implements ListsEvents {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lists);
		getSupportFragmentManager().beginTransaction()
		                           .replace(R.id.activityRoot, ListListFragment.newInstance(getExtraItemID()))
		                           .commit();
	}

	private long getExtraItemID() {
		return Intents.getItemFrom(getIntent());
	}

	@Override public void listSelected(long listID) {
		App.db().addListEntry(listID, getExtraItemID()); // FIXME DB on UI
		App.toast("Item has been added to a list.");
		getFragment().refresh();
	}
	@Override public void listRemoved(long listID) {
		App.db().deleteListEntry(listID, getExtraItemID()); // FIXME DB on UI
		App.toast("Item has been removed from a list.");
		getFragment().refresh();
	}

	private BaseFragment getFragment() {
		return (BaseFragment)getSupportFragmentManager().findFragmentById(R.id.activityRoot);
	}

	public static Intent manage(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ListsActivity.class);
		intent.putExtra(Extras.ITEM_ID, itemID);
		return intent;
	}
}
