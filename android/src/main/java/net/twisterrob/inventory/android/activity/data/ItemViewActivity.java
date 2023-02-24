package net.twisterrob.inventory.android.activity.data;

import android.annotation.SuppressLint;
import android.content.Intent;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Item;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.fragment.data.ItemViewFragment.ItemEvents;

public class ItemViewActivity extends BaseDetailActivity<ItemListFragment>
		implements ItemEvents, ItemsEvents {
	private ItemDTO current;

	public ItemViewActivity() {
		super(R.plurals.item);
	}

	@Override protected ItemListFragment onCreateFragment() {
		return ItemListFragment.newInstance(getExtraItemID()).addHeader(getIntent().getExtras());
	}

	public void itemLoaded(ItemDTO item) {
		setActionBarTitle(item.name);
		current = item;
	}

	public void itemDeleted(ItemDTO item) {
		finish();
	}

	public void newItem(long parentID) {
		startActivity(ItemEditActivity.add(parentID));
	}

	public void itemSelected(long id) {
		startActivity(Intents.childNav(ItemViewActivity.show(id)));
		// CONSIDER tabs as breadcrumbs?
	}

	public void itemActioned(long id) {
		startActivity(ItemEditActivity.edit(id));
	}

	@Override protected String checkExtras() {
		if (getExtraItemID() == Item.ID_ADD) {
			return "Invalid item ID";
		}
		return null;
	}

	private long getExtraItemID() {
		Intent intent = getIntent();
		long uri = InventoryContract.Item.getID(intent.getData());
		long extra = intent.getLongExtra(Extras.PARENT_ID, Item.ID_ADD);
		return resolve(uri, extra, Item.ID_ADD);
	}

	@Override public Intent getSupportParentActivityIntent() {
		if (current == null) {
			return null;
		}
		if (current.parentID == current.roomRoot || current.id == current.roomRoot) {
			return RoomViewActivity.show(current.room);
		} else {
			return ItemViewActivity.show(current.parentID);
		}
	}

	@Override public void onContentChanged() {
		super.onContentChanged();
		setupTitleEditor();
	}

	@SuppressLint({"WrongThread", "WrongThreadInterprocedural"}) // FIXME DB on UI
	@Override protected void updateName(String newName) {
		App.db().updateItem(current.id, current.type, newName, current.description);
	}

	/** Sanity checks and uri takes precedence over extra. */
	private static long resolve(long uri, long extra, long invalid) {
		if (uri == invalid && extra == invalid) {
			return invalid;
		}
		if (uri != invalid && extra != invalid && uri != extra) {
			throw new IllegalArgumentException(
					"Inconclusive ID resolution: URI (" + uri + ") and extras (" + extra + "), invalid = " + invalid);
		}
		if (uri != invalid) {
			return uri;
		}
		// if execution reaches this uri == invalid and extra must be valid, because the first if didn't execute
		//noinspection ConstantConditions can be simplified, but more readable this way
		if (extra != invalid) {
			return extra;
		}
		return invalid;
	}

	public static Intent show(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ItemViewActivity.class);
		intent.putExtra(Extras.PARENT_ID, itemID);
		return intent;
	}
}
