package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.CategoryViewFragment.CategoryEvents;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class CategoryItemsActivity extends BaseDetailActivity<CategoryViewFragment, ItemListFragment>
		implements CategoryEvents, ItemsEvents {
	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		setIcon(R.raw.category_unknown);
		long categoryID = getExtraCategoryID();
		CategoryViewFragment detailsFragment;
		if (categoryID == Category.INTERNAL) {
			detailsFragment = null;
			setActionBarSubtitle(null);
			setActionBarTitle(getText(R.string.item_list));
			hideDetails();
		} else {
			detailsFragment = CategoryViewFragment.newInstance(categoryID);
		}
		setFragments(detailsFragment, ItemListFragment.newCategoryInstance(categoryID, getExtraIncludeSubs()));
	}

	public void categoryLoaded(CategoryDTO category) {
		// ignore
	}

	public void itemSelected(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}

	public void itemActioned(long itemID) {
		startActivity(ItemEditActivity.edit(itemID));
	}

	public void newItem(long parentID) {
		// ignore
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
	public static Intent show(long categoryID) {
		Intent intent = new Intent(App.getAppContext(), CategoryItemsActivity.class);
		intent.putExtra(Extras.CATEGORY_ID, categoryID);
		intent.putExtra(Extras.INCLUDE_SUBS, true);
		return intent;
	}
	public static Intent showDirect(long categoryID) {
		Intent intent = new Intent(App.getAppContext(), CategoryItemsActivity.class);
		intent.putExtra(Extras.CATEGORY_ID, categoryID);
		intent.putExtra(Extras.INCLUDE_SUBS, false);
		return intent;
	}
}
