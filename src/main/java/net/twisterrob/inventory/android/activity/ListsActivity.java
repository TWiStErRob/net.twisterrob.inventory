package net.twisterrob.inventory.android.activity;

import android.content.Intent;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.fragment.ListListFragment;
import net.twisterrob.inventory.android.fragment.ListListFragment.ListsEvents;

public class ListsActivity extends SingleFragmentActivity<ListListFragment> implements ListsEvents {
	@Override protected ListListFragment onCreateFragment() {
		return ListListFragment.newInstance(getExtraItemID());
	}

	private long getExtraItemID() {
		return Intents.getItemFrom(getIntent());
	}

	@Override public void listSelected(long listID) {
		//noinspection WrongThread FIXME DB on UI
		App.db().addListEntry(listID, getExtraItemID());
		App.toastUser("Item has been added to a list.");
		getFragment().refresh();
	}
	@Override public void listRemoved(long listID) {
		//noinspection WrongThread FIXME DB on UI
		App.db().deleteListEntry(listID, getExtraItemID());
		App.toastUser("Item has been removed from a list.");
		getFragment().refresh();
	}

	public static Intent manage(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ListsActivity.class);
		intent.putExtra(Extras.ITEM_ID, itemID);
		return intent;
	}
}
