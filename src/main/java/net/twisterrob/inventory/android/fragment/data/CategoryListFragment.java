package net.twisterrob.inventory.android.fragment.data;

import android.os.Bundle;
import android.support.v7.widget.*;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.CategoryItemsActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.CategoryListFragment.CategoriesEvents;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.CategoryAdapter.CategoryItemEvents;

public class CategoryListFragment extends BaseFragment<CategoriesEvents> implements CategoryItemEvents {

	private RecyclerViewLoadersController listController;

	public interface CategoriesEvents {
		void categorySelected(long categoryID);
		void categoryActioned(long categoryID);
	}

	private HeaderManager header = null;

	public CategoryListFragment() {
		setDynamicResource(DYN_EventsClass, CategoriesEvents.class);
	}

	public void setHeader(BaseFragment headerFragment) {
		this.header = headerFragment != null? new HeaderManager(this, headerFragment) : null;
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new RecyclerViewLoadersController(this, Loaders.Categories) {
			@Override protected CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				CategoryAdapter cursorAdapter = new CategoryAdapter(null, CategoryListFragment.this);
				RecyclerView.Adapter adapter = cursorAdapter;
				if (header != null) {
					adapter = header.wrap(adapter);
				}
				list.setAdapter(adapter);
				return cursorAdapter;
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
		listController.setView(view);
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
		if (header != null) {
			header.getHeader().refresh();
		}
		listController.refresh();
	}

	@Override public void onItemClick(RecyclerView.ViewHolder holder) {
		eventsListener.categorySelected(holder.getItemId());
	}

	@Override public boolean onItemLongClick(RecyclerView.ViewHolder holder) {
		eventsListener.categoryActioned(holder.getItemId());
		return true;
	}

	@Override public void showItemsInCategory(ViewHolder holder) {
		getActivity().startActivity(CategoryItemsActivity.show(holder.getItemId()));
	}

	public static CategoryListFragment newInstance(long parentCategoryID) {
		CategoryListFragment fragment = new CategoryListFragment();
		fragment.setArguments(ExtrasFactory.bundleFromParent(parentCategoryID));
		return fragment;
	}
}
