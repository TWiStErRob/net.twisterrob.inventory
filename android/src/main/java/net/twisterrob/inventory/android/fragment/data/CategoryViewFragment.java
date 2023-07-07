package net.twisterrob.inventory.android.fragment.data;

import javax.inject.Inject;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.utils.tools.ResourceTools;
import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.android.utils.tools.ViewTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.content.model.CategoryVisuals;
import net.twisterrob.inventory.android.fragment.data.CategoryViewFragment.CategoryEvents;

import static net.twisterrob.inventory.android.content.Loaders.*;

@AndroidEntryPoint
public class CategoryViewFragment extends BaseViewFragment<CategoryDTO, CategoryEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryViewFragment.class);

	public interface CategoryEvents {
		void categoryLoaded(CategoryDTO item);
	}

	@Inject CategoryVisuals visuals;

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
				Intents.bundleFromCategory(getArgCategoryID()),
				SingleCategory.createCallbacks(requireContext(), new SingleRowLoaded())
		);
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
				.append("Category Name", ResourceTools.getText(requireContext(), entity.name))
				.append("Category Image", entity.typeImage, DEBUG)
				.append("Description", visuals.getDescription(entity.name))
				.append("Parent ID", entity.parentID, DEBUG)
				.append("Inside", entity.parentName)
				.append("# of direct subcategories", entity.numDirectChildren)
				.append("# of subcategories", entity.numAllChildren)
				.append("# of items in this category", entity.numDirectItems)
				.append("# of items in subcategories", entity.numAllItems)
				.append("Keywords", visuals.getKeywords(entity.name))
				.append("Extended Keywords", visuals.getKeywordsExtended(entity.name), DEBUG)
				.build();
	}

	@Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
		super.onPrepareOptionsMenu(menu);
		ViewTools.visibleIf(menu, R.id.action_category_viewAllItems, !getArgFlatten());
	}

	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
		return requireArguments().getLong(Extras.CATEGORY_ID, Category.ID_ADD);
	}

	private boolean getArgFlatten() {
		return requireArguments().getBoolean(Extras.INCLUDE_SUBS, false);
	}

	public static CategoryViewFragment newInstance(long categoryID, boolean flatten) {
		if (categoryID == Category.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing category");
		}

		CategoryViewFragment fragment = new CategoryViewFragment();
		Bundle args = Intents.bundleFromCategory(categoryID);
		args.putBoolean(Extras.INCLUDE_SUBS, flatten);
		fragment.setArguments(args);
		return fragment;
	}
}
