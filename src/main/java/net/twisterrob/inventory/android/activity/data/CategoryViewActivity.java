package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.data.CategoryActionsFragment.CategoryEvents;
import net.twisterrob.inventory.android.fragment.data.CategoryFragment;
import net.twisterrob.inventory.android.fragment.data.CategoryFragment.CategoriesEvents;

public class CategoryViewActivity extends BaseDetailActivity<CategoryFragment>
		implements CategoryEvents, CategoriesEvents {
	@Override protected void onCreate(Bundle savedInstanceState) {
		wantDrawer = getExtraParentCategoryID() == Category.INTERNAL;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected CategoryFragment onCreateFragment(Bundle savedInstanceState) {
		long parentID = getExtraParentCategoryID();
		CategoryFragment fragment = CategoryFragment.newInstance(parentID);
		if (parentID == Category.INTERNAL) {
			setActionBarTitle(getText(R.string.category_list));
			setActionBarSubtitle(null);
		} else {
			// FIXME fragment.setHeader(CategoryActionsFragment.newInstance(parentID));
		}
		return fragment;
	}

	public void categoryLoaded(CategoryDTO category) {
		setActionBarTitle(category.name);
	}

	public void categorySelected(long id) {
		startActivity(CategoryViewActivity.show(id));
		// TODO consider tabs as breadcrumbs?
	}

	public void categoryActioned(long id) {
		startActivity(CategoryItemsActivity.showDirect(id));
	}

	private long getExtraParentCategoryID() {
		return getIntent().getLongExtra(Extras.PARENT_ID, Category.ID_ADD);
	}

	public static Intent listAll() {
		return show(Category.INTERNAL);
	}
	public static Intent show(long categoryID) {
		Intent intent = new Intent(App.getAppContext(), CategoryViewActivity.class);
		intent.putExtra(Extras.PARENT_ID, categoryID);
		return intent;
	}
	@Override public void newItem(long parentID) {
		// ignore
	}
	@Override public void itemSelected(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}
	@Override public void itemActioned(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}
}
