package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.data.CategoryViewFragment.CategoryEvents;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class CategoryViewFragment extends BaseViewFragment<CategoryDTO, CategoryEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryViewFragment.class);

	public interface CategoryEvents {
		void categoryLoaded(CategoryDTO item);
	}

	public CategoryViewFragment() {
		setDynamicResource(DYN_EventsClass, CategoryEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.category);
	}

	@Override protected void onRefresh() {
		super.onRefresh();
		getLoaderManager().getLoader(SingleCategory.id()).onContentChanged();
	}

	@Override protected void onStartLoading() {
		super.onStartLoading();
		getLoaderManager().initLoader(SingleCategory.id(),
				Intents.bundleFromCategory(getArgCategoryID()), new SingleRowLoaded());
	}

	@Override protected void onSingleRowLoaded(@NonNull Cursor cursor) {
		CategoryDTO category = CategoryDTO.fromCursor(cursor);
		super.onSingleRowLoaded(category);
		eventsListener.categoryLoaded(category);
	}

	@Override protected CharSequence getDetailsString(CategoryDTO entity, boolean DEBUG) {
		return new DescriptionBuilder()
				.append("Category ID", entity.id, DEBUG)
				.append("Category Key", entity.name, DEBUG)
				.append("Category Name", AndroidTools.getText(getContext(), entity.name))
				.append("Category Image", entity.typeImage, DEBUG)
				.append("Description", CategoryDTO.getDescription(getContext(), entity.name))
				.append("Parent ID", entity.parentID, DEBUG)
				.append("Inside", entity.parentName)
				.append("# of direct subcategories", entity.numDirectChildren)
				.append("# of subcategories", entity.numAllChildren)
				.append("# of items in this category", entity.numDirectItems)
				.append("# of items in subcategories", entity.numAllItems)
				.append("Keywords", CategoryDTO.getKeywords(getContext(), entity.name))
				.append("Extended Keywords", CategoryDTO.getKeywordsExtended(getContext(), entity.name), DEBUG)
				.build();
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_category_viewAllItems:
				startActivity(CategoryActivity.showFlattened(getArgCategoryID()));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected void editImage() {
		throw new UnsupportedOperationException("Can't edit image of category");
	}

	private long getArgCategoryID() {
		return getArguments().getLong(Extras.CATEGORY_ID, Category.ID_ADD);
	}

	public static CategoryViewFragment newInstance(long categoryID) {
		if (categoryID == Category.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing category");
		}

		CategoryViewFragment fragment = new CategoryViewFragment();
		fragment.setArguments(Intents.bundleFromCategory(categoryID));
		return fragment;
	}
}
