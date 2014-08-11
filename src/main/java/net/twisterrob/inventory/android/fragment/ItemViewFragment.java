package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.ItemViewFragment.ItemEvents;
import net.twisterrob.inventory.android.tasks.DeleteItemTask;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemViewFragment extends BaseViewFragment<ItemEvents> {
	public interface ItemEvents {
		void itemLoaded(ItemDTO item);
		void itemDeleted(ItemDTO item);
	}

	public ItemViewFragment() {
		setDynamicResource(DYN_EventsClass, ItemEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.item);
	}

	@Override
	protected void onRefresh() {
		getLoaderManager().getLoader(SingleItem.ordinal()).forceLoad();
	}

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.ITEM_ID, getArgItemID());
		getLoaderManager().initLoader(SingleItem.ordinal(), args, new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		ItemDTO item = ItemDTO.fromCursor(cursor);

		setTitle(item.name);
		Drawable fallback = item.getFallbackDrawable(getActivity());
		setIcon(fallback);
		App.pic().load(item.image).placeholder(fallback).into(image);

		eventsListener.itemLoaded(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_edit:
				startActivity(ItemEditActivity.edit(getArgItemID()));
				return true;
			case R.id.action_item_delete:
				delete(getArgItemID());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void delete(final long itemID) {
		new DeleteItemTask(itemID, new Dialogs.Callback() {
			public void dialogSuccess() {
				ItemDTO item = new ItemDTO();
				item.id = itemID;
				eventsListener.itemDeleted(item);
			}

			public void dialogFailed() {
				Toast.makeText(getActivity(), "Cannot delete item #" + itemID, Toast.LENGTH_LONG).show();
			}
		}).displayDialog(getActivity());
	}

	private long getArgItemID() {
		return getArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	public static ItemViewFragment newInstance(long itemID) {
		if (itemID == Item.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing item");
		}

		ItemViewFragment fragment = new ItemViewFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.ITEM_ID, itemID);

		fragment.setArguments(args);
		return fragment;
	}
}
