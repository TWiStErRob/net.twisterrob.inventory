package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.view.MenuItem;
import android.widget.Toast;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.BaseSingleLoaderFragment;
import net.twisterrob.inventory.android.fragment.data.CategoryActionsFragment.CategoryEvents;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class CategoryActionsFragment extends BaseSingleLoaderFragment<CategoryEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryActionsFragment.class);

	public interface CategoryEvents {
		void categoryLoaded(CategoryDTO item);
	}

	public CategoryActionsFragment() {
		setDynamicResource(DYN_EventsClass, CategoryEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.category);
	}

	@Override
	protected void onRefresh() {
		super.onRefresh();
		getLoaderManager().getLoader(SingleCategory.ordinal()).onContentChanged();
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		getLoaderManager().initLoader(SingleCategory.ordinal(),
				ExtrasFactory.bundleFromCategory(getArgCategoryID()), new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		CategoryDTO item = CategoryDTO.fromCursor(cursor);
		eventsListener.categoryLoaded(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_category_viewAllItems:
				startActivity(CategoryActivity.showFlattened(getArgCategoryID()));
				return true;
			case R.id.action_share:
				Toast.makeText(getContext(), "Not implemented yet", Toast.LENGTH_LONG).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private long getArgCategoryID() {
		return getArguments().getLong(Extras.CATEGORY_ID, Category.ID_ADD);
	}

	public static CategoryActionsFragment newInstance(long categoryID) {
		if (categoryID == Category.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing category");
		}

		CategoryActionsFragment fragment = new CategoryActionsFragment();
		fragment.setArguments(ExtrasFactory.bundleFromCategory(categoryID));
		return fragment;
	}
}
