package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.CategoryViewFragment.CategoryEvents;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class CategoryViewFragment extends BaseViewFragment<CategoryEvents> {
	public interface CategoryEvents {
		void categoryLoaded(CategoryDTO item);
	}

	public CategoryViewFragment() {
		setDynamicResource(DYN_EventsClass, CategoryEvents.class);
	}

	@Override
	protected void onRefresh() {
		getLoaderManager().getLoader(SingleCategory.ordinal()).forceLoad();
	}

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.CATEGORY_ID, getArgCategoryID());
		getLoaderManager().initLoader(SingleCategory.ordinal(), args, new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		CategoryDTO item = CategoryDTO.fromCursor(cursor);

		getActivity().setTitle(item.name);
		title.setText(item.name);
		App.pic().load(item.image).placeholder(item.getFallbackDrawableID(getActivity())).into(image);

		eventsListener.categoryLoaded(item);
	}

	private long getArgCategoryID() {
		return getArguments().getLong(Extras.CATEGORY_ID, Category.ID_ADD);
	}

	public static CategoryViewFragment newInstance(long categoryID) {
		if (categoryID == Category.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing category");
		}

		CategoryViewFragment fragment = new CategoryViewFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.CATEGORY_ID, categoryID);

		fragment.setArguments(args);
		return fragment;
	}
}
