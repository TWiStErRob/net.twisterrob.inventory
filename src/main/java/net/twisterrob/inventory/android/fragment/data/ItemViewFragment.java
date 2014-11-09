package net.twisterrob.inventory.android.fragment.data;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.ItemEditActivity;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.data.ItemViewFragment.ItemEvents;
import net.twisterrob.inventory.android.tasks.DeleteItemTask;
import net.twisterrob.inventory.android.view.Dialogs;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemViewFragment extends BaseViewFragment<ItemDTO, ItemEvents> {
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
		super.onSingleRowLoaded(item);
		eventsListener.itemLoaded(item);
	}

	@Override
	protected CharSequence getDetailsString(ItemDTO entity) {
		return new DescriptionBuilder() //
				.append("Item name", entity.name) //
				.append("Category", AndroidTools.getText(getActivity(), entity.categoryName)) //
				.append("Inside", entity.parentName != null? entity.parentName : "the room") //
				.append("Room", entity.roomName) //
				.append("Property", entity.propertyName) //
				.append("# of items in this item", entity.numDirectItems) //
				.append("# of items inside", entity.numAllItems) //
				.build();
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
				App.toast("Cannot delete item #" + itemID);
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
