package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;

import androidx.annotation.Nullable;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.utils.tools.ResourceTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.data.CategoryContentsFragment;
import net.twisterrob.inventory.android.fragment.data.CategoryContentsFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.data.CategoryViewFragment.CategoryEvents;

@AndroidEntryPoint
public class CategoryActivity extends BaseDetailActivity<CategoryContentsFragment>
		implements CategoryEvents, CategoriesEvents {
	private CategoryDTO current;

	public CategoryActivity() {
		super(R.plurals.category);
	}

	@Override protected CategoryContentsFragment onCreateFragment() {
		return CategoryContentsFragment.newInstance(getExtraCategoryID(), getExtraIncludeSubs()).addHeader();
	}

	public void categoryLoaded(CategoryDTO category) {
		current = category;
		setActionBarTitle(ResourceTools.getText(this, category.name));
	}
	public void categorySelected(long id) {
		startActivity(Intents.childNav(CategoryActivity.show(id)));
		// CONSIDER tabs as breadcrumbs?
	}
	public void categoryActioned(long id) {
		startActivity(Intents.childNav(CategoryActivity.show(id)));
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

	@Override protected String checkExtras() {
		Long id = getExtraCategoryID();
		if (id != null && id == Category.ID_ADD) {
			return "Invalid category ID";
		}
		return null;
	}

	@Override public boolean onSupportNavigateUp() {
		if (getExtraCategoryID() == null) {
			onBackPressed();
			return true;
		}
		return super.onSupportNavigateUp();
	}

	@Override public Intent getSupportParentActivityIntent() {
		if (current != null) {
			if (current.parentID != null) {
				return CategoryActivity.show(current.parentID);
			} else {
				return CategoryActivity.show(null);
			}
		}
		return null;
	}

	private @Nullable Long getExtraCategoryID() {
		return Intents.getOptionalCategory(getIntent().getExtras());
	}

	private boolean getExtraIncludeSubs() {
		return getIntent().getBooleanExtra(Extras.INCLUDE_SUBS, false);
	}

	public static Intent show(Long categoryID) {
		return show(categoryID, false);
	}
	public static Intent showFlattened(Long categoryID) {
		return show(categoryID, true);
	}

	private static Intent show(Long categoryID, boolean flattened) {
		if (categoryID == null) {
			if (flattened) {
				return MainActivity.list(App.getAppContext(), MainActivity.PAGE_ITEMS);
			} else {
				return MainActivity.list(App.getAppContext(), MainActivity.PAGE_CATEGORIES);
			}
		}
		Intent intent = new Intent(App.getAppContext(), CategoryActivity.class);
		intent.putExtra(Extras.CATEGORY_ID, categoryID);
		intent.putExtra(Extras.INCLUDE_SUBS, flattened);
		return intent;
	}
}
