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
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.ItemListFragment.ItemsEvents;

public class ItemListFragment extends BaseListFragment<ItemsEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemListFragment.class);

	public interface ItemsEvents {
		void newItem(long parentID);
		void itemSelected(long itemID);
		void itemActioned(long itemID);
	}

	public ItemListFragment() {
		setDynamicResource(DYN_EventsClass, ItemsEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.item_list);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_item_add).setVisible(getArgParentItemID() != Item.ID_ADD);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_add:
				eventsListener.newItem(getArgParentItemID());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected View inflateRoot(LayoutInflater inflater, ViewGroup container) {
		if (getArgParentItemID() == Item.ID_ADD) {
			return inflater.inflate(R.layout.gallery_readonly, container, false);
		} else {
			return super.inflateRoot(inflater, container);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);

		view.findViewById(R.id.btn_add).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				eventsListener.newItem(getArgParentItemID());
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

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, getArgParentItemID());
		args.putLong(Extras.CATEGORY_ID, getArgCategoryID());
		args.putBoolean(Extras.INCLUDE_SUBS, getArgIncludeSubs());
		getLoaderManager().initLoader(Loaders.Items.ordinal(), args, createListLoaderCallbacks());
	}

	private long getArgParentItemID() {
		return getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
	}
	private long getArgCategoryID() {
		return getArguments().getLong(Extras.CATEGORY_ID, Category.ID_ADD);
	}
	private boolean getArgIncludeSubs() {
		return getArguments().getBoolean(Extras.INCLUDE_SUBS, false);
	}

	@Override
	protected void onRefresh() {
		getLoaderManager().getLoader(Loaders.Items.ordinal()).forceLoad();
	}

	public static ItemListFragment newInstance(long parentItemID) {
		ItemListFragment fragment = new ItemListFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, parentItemID);

		fragment.setArguments(args);
		return fragment;
	}

	public static ItemListFragment newCategoryInstance(long categoryID, boolean include) {
		ItemListFragment fragment = new ItemListFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.CATEGORY_ID, categoryID);
		args.putBoolean(Extras.INCLUDE_SUBS, include);

		fragment.setArguments(args);
		return fragment;
	}
}
