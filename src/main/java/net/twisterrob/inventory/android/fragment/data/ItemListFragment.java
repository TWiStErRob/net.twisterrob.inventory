package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class ItemListFragment extends BaseGalleryFragment<ItemsEvents> {
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
		menu.findItem(R.id.action_item_add).setVisible(canCreateNew());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_add:
				onCreateNew();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStartLoading() {
		CharSequence query = getArgQuery();
		if (query == null) {
			Bundle args = new Bundle();
			args.putLong(Extras.PARENT_ID, getArgParentItemID());
			args.putLong(Extras.CATEGORY_ID, getArgCategoryID());
			args.putBoolean(Extras.INCLUDE_SUBS, getArgIncludeSubs());
			getLoaderManager().initLoader(Loaders.Items.ordinal(), args, createListLoaderCallbacks());
		} else {
			Bundle args = new Bundle();
			args.putCharSequence(SearchManager.QUERY, query);
			getLoaderManager().initLoader(Loaders.ItemSearch.ordinal(), args, createListLoaderCallbacks());
		}
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
	private CharSequence getArgQuery() {
		return getArguments().getCharSequence(SearchManager.QUERY);
	}

	@Override protected void onRefresh() {
		if (getArgQuery() == null) {
			getLoaderManager().getLoader(Loaders.Items.ordinal()).forceLoad();
		} else {
			getLoaderManager().getLoader(Loaders.ItemSearch.ordinal()).forceLoad();
		}
	}

	@Override protected boolean canCreateNew() {
		return getArgParentItemID() != Item.ID_ADD;
	}

	@Override protected void onCreateNew() {
		eventsListener.newItem(getArgParentItemID());
	}

	@Override public void onItemClick(RecyclerView.ViewHolder holder) {
		eventsListener.itemSelected(holder.getItemId());
	}

	@Override public boolean onItemLongClick(RecyclerView.ViewHolder holder) {
		eventsListener.itemActioned(holder.getItemId());
		return true;
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

	public static ItemListFragment newSearchInstance(CharSequence query) {
		ItemListFragment fragment = new ItemListFragment();

		Bundle args = new Bundle();
		args.putCharSequence(SearchManager.QUERY, query);

		fragment.setArguments(args);
		return fragment;
	}
}
