package net.twisterrob.inventory.android.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.StrictMode;

import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.fragment.ListListFragment;
import net.twisterrob.inventory.android.fragment.ListListFragment.ListsEvents;

@AndroidEntryPoint
public class ListsActivity extends SingleFragmentActivity<ListListFragment> implements ListsEvents {
	@Override protected ListListFragment onCreateFragment() {
		return ListListFragment.newInstance(getExtraItemID());
	}

	private long getExtraItemID() {
		return Intents.getItemFrom(getIntent());
	}

	@SuppressLint({"WrongThread", "WrongThreadInterprocedural"}) // FIXME DB on UI
	@Override public void listSelected(long listID) {
		StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
		try {
			App.db().addListEntry(listID, getExtraItemID());
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
		App.toastUser("Item has been added to a list.");
		getFragment().refresh();
	}
	@SuppressLint({"WrongThread", "WrongThreadInterprocedural"}) // FIXME DB on UI
	@Override public void listRemoved(long listID) {
		StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
		try {
			App.db().deleteListEntry(listID, getExtraItemID());
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
		App.toastUser("Item has been removed from a list.");
		getFragment().refresh();
	}

	public static Intent manage(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ListsActivity.class);
		intent.putExtra(Extras.ITEM_ID, itemID);
		return intent;
	}
}
