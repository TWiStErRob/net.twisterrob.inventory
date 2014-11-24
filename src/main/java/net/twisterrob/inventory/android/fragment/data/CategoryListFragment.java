package net.twisterrob.inventory.android.fragment.data;

import android.os.Bundle;
import android.support.v7.widget.*;
import android.support.v7.widget.RecyclerView.ViewHolder;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.activity.data.CategoryItemsActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.data.CategoryListFragment.CategoriesEvents;
import net.twisterrob.inventory.android.view.CategoryAdapter;
import net.twisterrob.inventory.android.view.CategoryAdapter.CategoryItemEvents;

public class CategoryListFragment extends BaseRecyclerFragment<CategoriesEvents> implements CategoryItemEvents {
	public interface CategoriesEvents {
		void categorySelected(long categoryID);
		void categoryActioned(long categoryID);
	}

	public CategoryListFragment() {
		setDynamicResource(DYN_EventsClass, CategoriesEvents.class);
	}

	@Override protected CursorRecyclerAdapter setupList() {
		list.setLayoutManager(new LinearLayoutManager(getContext()));
		return new CategoryAdapter(null, this);
	}

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, getArgParentItemID());
		getLoaderManager().initLoader(Loaders.Categories.ordinal(), args, createListLoaderCallbacks());
	}

	private long getArgParentItemID() {
		return getArguments().getLong(Extras.PARENT_ID, Category.ID_ADD);
	}

	@Override
	protected void onRefresh() {
		getLoaderManager().getLoader(Loaders.Categories.ordinal()).forceLoad();
	}

	@Override protected boolean canCreateNew() {
		return false;
	}

	@Override protected void onCreateNew() {
		throw new UnsupportedOperationException("Cannot create new category, please send us an email if you miss one!");
	}

	@Override public void onItemClick(RecyclerView.ViewHolder holder) {
		eventsListener.categorySelected(holder.getItemId());
	}

	@Override public boolean onItemLongClick(RecyclerView.ViewHolder holder) {
		eventsListener.categoryActioned(holder.getItemId());
		return true;
	}

	@Override public void showItemsInCategory(ViewHolder holder) {
		getActivity().startActivity(CategoryItemsActivity.show(holder.getItemId()));
	}

	public static CategoryListFragment newInstance(long parentCategoryID) {
		CategoryListFragment fragment = new CategoryListFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, parentCategoryID);

		fragment.setArguments(args);
		return fragment;
	}
}
