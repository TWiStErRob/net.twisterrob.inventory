package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.data.CategoryContentsFragment;
import net.twisterrob.inventory.android.fragment.data.CategoryContentsFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.data.CategoryViewFragment.CategoryEvents;

public class CategoryActivity extends BaseDetailActivity<CategoryContentsFragment>
		implements CategoryEvents, CategoriesEvents {
	@Override
	protected CategoryContentsFragment onCreateFragment(Bundle savedInstanceState) {
		return CategoryContentsFragment.newInstance(getExtraCategoryID(), getExtraIncludeSubs()).addHeader();
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
		throw new UnsupportedOperationException("Cannot create new item here");
	}
	@Override public void itemSelected(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}
	@Override public void itemActioned(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}

	@Override
	protected String checkExtras() {
		if (getExtraCategoryID() == Category.ID_ADD) {
			return "Invalid category ID";
		}
		return null;
	}

	private long getExtraCategoryID() {
		return getIntent().getExtras().getLong(Extras.CATEGORY_ID, Category.ID_ADD);
	}

	private boolean getExtraIncludeSubs() {
		return getIntent().getBooleanExtra(Extras.INCLUDE_SUBS, false);
	}

	public static Intent show(long categoryID) {
		return show(categoryID, false);
	}
	public static Intent showFlattened(long categoryID) {
		return show(categoryID, true);
	}

	private static Intent show(long categoryID, boolean flattened) {
		Intent intent = new Intent(App.getAppContext(), CategoryActivity.class);
		intent.putExtra(Extras.CATEGORY_ID, categoryID);
		intent.putExtra(Extras.INCLUDE_SUBS, flattened);
		return intent;
	}
}
