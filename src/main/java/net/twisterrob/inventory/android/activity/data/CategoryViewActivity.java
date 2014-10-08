package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.CategoryListFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.data.CategoryViewFragment.CategoryEvents;

public class CategoryViewActivity extends BaseDetailActivity<CategoryViewFragment, CategoryListFragment>
		implements
			CategoryEvents,
			CategoriesEvents {
	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		long parentID = getExtraParentCategoryID();
		if (parentID == Category.INTERNAL) {
			hideDetails();
			getSupportActionBar().setTitle(R.string.category_list);
			getSupportActionBar().setIcon(App.pic().getSVG(this, R.raw.category_unknown));
		}
		setFragments(CategoryViewFragment.newInstance(parentID), CategoryListFragment.newInstance(parentID));
	}

	public void categoryLoaded(CategoryDTO category) {
		// ignore
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

	public static Intent show(long categoryID) {
		Intent intent = new Intent(App.getAppContext(), CategoryViewActivity.class);
		intent.putExtra(Extras.PARENT_ID, categoryID);
		return intent;
	}
}
