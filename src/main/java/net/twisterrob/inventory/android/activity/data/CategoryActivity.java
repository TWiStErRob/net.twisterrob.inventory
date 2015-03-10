package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.CategoryActionsFragment.CategoryEvents;
import net.twisterrob.inventory.android.fragment.data.CategoryFragment.CategoriesEvents;

public class CategoryActivity extends BaseDetailActivity<CategoryFragment>
		implements CategoryEvents, CategoriesEvents {
	@Override protected void onCreate(Bundle savedInstanceState) {
		long categoryID = getExtraCategoryID();
		wantDrawer = categoryID == Category.INTERNAL;
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			getSupportFragmentManager()
					.beginTransaction()
					.add(CategoryActionsFragment.newInstance(categoryID), "details")
					.commit()
			;
		}
	}

	@Override
	protected CategoryFragment onCreateFragment(Bundle savedInstanceState) {
		long categoryID = getExtraCategoryID();
		CategoryFragment fragment = CategoryFragment.newInstance(categoryID, getExtraIncludeSubs());
		return fragment;
	}

	public void categoryLoaded(CategoryDTO category) {
		if (category.id == Category.INTERNAL) {
			setActionBarTitle(getText(getExtraIncludeSubs()? R.string.item_list : R.string.category_list));
			setActionBarSubtitle(null);
		} else {
			setActionBarTitle(AndroidTools.getText(this, category.name));
		}
	}

	public void categorySelected(long id) {
		startActivity(CategoryActivity.show(id));
		// TODO consider tabs as breadcrumbs?
	}

	public void categoryActioned(long id) {
		startActivity(CategoryActivity.show(id));
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

	private long getExtraCategoryID() {
		return getIntent().getLongExtra(Extras.CATEGORY_ID, Category.ID_ADD);
	}

	private boolean getExtraIncludeSubs() {
		return getIntent().getBooleanExtra(Extras.INCLUDE_SUBS, false);
	}

	public static Intent listAll() {
		return show(Category.INTERNAL);
	}
	public static Intent listAllItems() {
		return showFlattened(Category.INTERNAL);
	}

	public static Intent show(long categoryID) {
		Intent intent = new Intent(App.getAppContext(), CategoryActivity.class);
		intent.putExtra(Extras.CATEGORY_ID, categoryID);
		intent.putExtra(Extras.INCLUDE_SUBS, false);
		return intent;
	}
	public static Intent showFlattened(long categoryID) {
		Intent intent = new Intent(App.getAppContext(), CategoryActivity.class);
		intent.putExtra(Extras.CATEGORY_ID, categoryID);
		intent.putExtra(Extras.INCLUDE_SUBS, true);
		return intent;
	}
}
