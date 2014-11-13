package net.twisterrob.inventory.android.fragment.data;

import android.database.Cursor;
import android.os.Bundle;
import android.view.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.CategoryItemsActivity;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.data.CategoryViewFragment.CategoryEvents;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class CategoryViewFragment extends BaseViewFragment<CategoryDTO, CategoryEvents> {
	private CharSequence nameCache;

	public interface CategoryEvents {
		void categoryLoaded(CategoryDTO item);
	}

	public CategoryViewFragment() {
		setDynamicResource(DYN_EventsClass, CategoryEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.category);
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

		if (item.id != Category.INTERNAL) {
			nameCache = item.name = AndroidTools.getText(getContext(), item.name).toString();
			super.onSingleRowLoaded(item);
		}

		eventsListener.categoryLoaded(item);
	}

	@Override
	protected CharSequence getDetailsString(CategoryDTO entity) {
		return new DescriptionBuilder() //
				.append("Category name", entity.name) //
				.append("Inside", entity.parentName) //
				.append("# of direct subcategories", entity.numDirectChildren) //
				.append("# of all subcategories", entity.numAllChildren) //
				.append("# of items in this category", entity.numDirectItems) //
				.append("# of items inside", entity.numAllItems) //
				.build();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		boolean isRoot = getArgCategoryID() == Category.INTERNAL;
		MenuItem share = menu.findItem(R.id.action_share);
		MenuItem viewItems = menu.findItem(R.id.action_category_viewItems);
		MenuItem viewAllItems = menu.findItem(R.id.action_category_viewAllItems);

		if (nameCache != null) {
			viewItems.setTitle(getString(R.string.category_viewItems_special, nameCache));
			viewAllItems.setTitle(getString(R.string.category_viewAllItems_special, nameCache));
		}

		share.setVisible(!isRoot);
		viewItems.setVisible(!isRoot);
		viewAllItems.setVisible(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_category_viewItems:
				startActivity(CategoryItemsActivity.showDirect(getArgCategoryID()));
				return true;
			case R.id.action_category_viewAllItems:
				startActivity(CategoryItemsActivity.show(getArgCategoryID()));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
