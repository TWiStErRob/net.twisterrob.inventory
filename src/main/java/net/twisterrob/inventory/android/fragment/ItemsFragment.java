package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.Extras;
import net.twisterrob.inventory.android.fragment.ItemsFragment.ItemEvents;

public class ItemsFragment extends BaseListFragment<ItemEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemsFragment.class);

	public interface ItemEvents {
		void newItem();
		void itemSelected(long itemID);
		void itemActioned(long itemID);
	}

	public ItemsFragment() {
		setDynamicResource(DYN_EventsClass, ItemEvents.class);
		setDynamicResource(DYN_Layout, R.layout.item_coll);
		setDynamicResource(DYN_List, R.id.items);
		setDynamicResource(DYN_CursorAdapter, R.xml.items);
		setDynamicResource(DYN_OptionsMenu, R.menu.items);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_add:
				eventsListener.newItem();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);

		getView().findViewById(R.id.btn_add).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				eventsListener.newItem();
			}
		});

		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				eventsListener.itemActioned(id);
				return true;
			}
		});
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				eventsListener.itemSelected(id);
			}
		});
	}

	public void list(long parentItemID) {
		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, parentItemID);
		getLoaderManager().initLoader(Loaders.Items.ordinal(), args, createListLoaderCallbacks());
	}

	public void refresh() {
		getLoaderManager().getLoader(Loaders.Items.ordinal()).forceLoad();
	}
}
