package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.Extras;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.CategoryActionsFragment.CategoryEvents;
import net.twisterrob.inventory.android.fragment.data.CategoryFragment.CategoriesEvents;

public class CategoryActivity extends BaseDetailActivity<CategoryFragment>
		implements CategoryEvents, CategoriesEvents {
	@Override protected void onCreate(Bundle savedInstanceState) {
		Long categoryID = getExtraCategoryID();
		wantDrawer = categoryID == null;
		super.onCreate(savedInstanceState);
		if (categoryID == null) {
			setActionBarTitle(getText(getExtraIncludeSubs()? R.string.item_list : R.string.category_list));
			setActionBarSubtitle(null);
		} else if (savedInstanceState == null) {
			getSupportFragmentManager()
					.beginTransaction()
					.add(CategoryActionsFragment.newInstance(categoryID), "details")
					.commit()
			;
		}
	}

	@Override
	protected CategoryFragment onCreateFragment(Bundle savedInstanceState) {
		Long categoryID = getExtraCategoryID();
		CategoryFragment fragment = CategoryFragment.newInstance(categoryID, getExtraIncludeSubs());
		return fragment;
	}

	public void categoryLoaded(CategoryDTO category) {
		setActionBarTitle(AndroidTools.getText(this, category.name));
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

	private Long getExtraCategoryID() {
		return (Long)getIntent().getExtras().get(Extras.CATEGORY_ID);
	}

	private boolean getExtraIncludeSubs() {
		return getIntent().getBooleanExtra(Extras.INCLUDE_SUBS, false);
	}

	public static Intent listAll() {
		return show(null, false);
	}
	public static Intent listAllItems() {
		return show(null, true);
	}
	public static Intent show(long categoryID) {
		return show(categoryID, false);
	}
	public static Intent showFlattened(long categoryID) {
		return show(categoryID, true);
	}

	private static Intent show(Long categoryID, boolean flattened) {
		Intent intent = new Intent(App.getAppContext(), CategoryActivity.class);
		intent.putExtra(Extras.CATEGORY_ID, categoryID);
		intent.putExtra(Extras.INCLUDE_SUBS, flattened);
		return intent;
	}
}
