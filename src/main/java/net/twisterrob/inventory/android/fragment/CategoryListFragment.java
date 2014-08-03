package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.CategoryListFragment.CategoriesEvents;

public class CategoryListFragment extends BaseListFragment<CategoriesEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryListFragment.class);

	public interface CategoriesEvents {
		void categorySelected(long categoryID);
		void categoryActioned(long categoryID);
	}

	public CategoryListFragment() {
		setDynamicResource(DYN_EventsClass, CategoriesEvents.class);
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);

		getView().findViewById(R.id.btn_add).setVisibility(View.GONE);

		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				eventsListener.categoryActioned(id);
				return true;
			}
		});
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				eventsListener.categorySelected(id);
			}
		});
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

	public static CategoryListFragment newInstance(long parentCategoryID) {
		CategoryListFragment fragment = new CategoryListFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, parentCategoryID);

		fragment.setArguments(args);
		return fragment;
	}
}
