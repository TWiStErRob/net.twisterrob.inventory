package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.os.Bundle;
import android.support.v7.widget.*;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.CategoryItemsActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.CategoryFragment.CategoriesEvents;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.CategoryAndItemsAdapter.CategoryItemEvents;

public class CategoryFragment extends BaseFragment<CategoriesEvents> implements CategoryItemEvents {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryFragment.class);

	private RecyclerViewLoadersController listController;

	public interface CategoriesEvents {
		void categorySelected(long categoryID);
		void categoryActioned(long categoryID);
	}

	public CategoryFragment() {
		setDynamicResource(DYN_EventsClass, CategoriesEvents.class);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new RecyclerViewLoadersController(this, Loaders.Categories) {
			@Override protected CursorRecyclerAdapter setupList() {
				CategoryAndItemsAdapter adapter = new CategoryAndItemsAdapter(CategoryFragment.this);
				GridLayoutManager layout = (GridLayoutManager)adapter.createLayout(getContext());

				HeaderViewRecyclerAdapter headerAdapter = new HeaderViewRecyclerAdapter(adapter);
				layout.setSpanSizeLookup(headerAdapter.wrap(layout.getSpanSizeLookup(), layout.getSpanCount()));
				// TODO header

				list.setLayoutManager(layout);
				list.setAdapter(headerAdapter);
				return adapter;
			}

			@Override public boolean canCreateNew() {
				return false;
			}

			@Override protected void onCreateNew() {
				throw new UnsupportedOperationException(
						"Cannot create new category, please send us an email if you miss one!");
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.generic_list, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override
	protected void onStartLoading() {
		listController.startLoad(ExtrasFactory.bundleFromParent(getArgParentItemID()));
	}

	private long getArgParentItemID() {
		return getArguments().getLong(Extras.PARENT_ID, Category.ID_ADD);
	}

	@Override
	protected void onRefresh() {
		listController.refresh();
	}

	@Override public void onItemClick(int position, long recyclerViewItemID) {
		eventsListener.categorySelected(recyclerViewItemID);
	}

	@Override public boolean onItemLongClick(int position, long recyclerViewItemID) {
		eventsListener.categoryActioned(recyclerViewItemID);
		return true;
	}

	@Override public void showItemsInCategory(long categoryID) {
		getActivity().startActivity(CategoryItemsActivity.show(categoryID));
	}

	public static CategoryFragment newInstance(long parentCategoryID) {
		CategoryFragment fragment = new CategoryFragment();
		fragment.setArguments(ExtrasFactory.bundleFromParent(parentCategoryID));
		return fragment;
	}
}
