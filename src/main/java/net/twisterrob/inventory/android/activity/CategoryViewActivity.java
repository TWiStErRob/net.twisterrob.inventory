package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.CategoryListFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.CategoryViewFragment.CategoryEvents;

public class CategoryViewActivity extends BaseDetailActivity<CategoryViewFragment, CategoryListFragment>
		implements
			CategoryEvents,
			CategoriesEvents {
	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		long parentID = getExtraParentItemID();
		CategoryViewFragment detailsFragment;
		if (parentID == Category.INTERNAL) {
			detailsFragment = null;
			hideDetails();
		} else {
			detailsFragment = CategoryViewFragment.newInstance(parentID);
		}
		setFragments(detailsFragment, CategoryListFragment.newInstance(parentID));
	}

	public void categoryLoaded(CategoryDTO category) {
		// ignore
	}

	public void categorySelected(long id) {
		startActivity(CategoryViewActivity.show(id));
		// TODO consider tabs as breadcrumbs?
	}

	public void categoryActioned(long id) {
		startActivity(CategoryViewActivity.show(id));
	}

	private long getExtraParentItemID() {
		return getIntent().getLongExtra(Extras.PARENT_ID, Category.ID_ADD);
	}

	public static Intent show(long categoryID) {
		Intent intent = new Intent(App.getAppContext(), CategoryViewActivity.class);
		intent.putExtra(Extras.PARENT_ID, categoryID);
		return intent;
	}
}
