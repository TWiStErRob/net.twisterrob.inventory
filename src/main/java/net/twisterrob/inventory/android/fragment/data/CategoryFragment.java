package net.twisterrob.inventory.android.fragment.data;

import java.io.File;

import org.slf4j.*;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.*;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.view.*;

import net.twisterrob.android.adapter.ConcatAdapter;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.ImageActivity;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.CategoryDTO;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.CategoryFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.CategoryAdapter.CategoryItemEvents;
import net.twisterrob.inventory.android.view.DetailsAdapter.DetailsEvents;
import net.twisterrob.inventory.android.view.GalleryAdapter.GalleryItemEvents;

public class CategoryFragment extends BaseFragment<CategoriesEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryFragment.class);

	private RecyclerViewCursorsLoadersController listController;

	public interface CategoriesEvents extends ItemsEvents {
		void categorySelected(long categoryID);
		void categoryActioned(long categoryID);
	}

	public CategoryFragment() {
		setDynamicResource(DYN_EventsClass, CategoriesEvents.class);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new RecyclerViewCursorsLoadersController(getContext(), getLoaderManager(),
				Loaders.SingleCategory, Loaders.Categories, Loaders.Items) {
			@Override protected void setupList() {
				final DetailsAdapter headerAdapter = new DetailsAdapter(new DetailsEventsForwarder()) {
					@Override
					protected CharSequence getDetailsString(Context context, Cursor cursor) {
						CategoryDTO entity = CategoryDTO.fromCursor(cursor);
						final boolean DEBUG = App.getPrefs().getBoolean(getString(R.string.pref_displayDebugDetails),
								getResources().getBoolean(R.bool.pref_displayDebugDetails_default));
						return new DescriptionBuilder()
								.append("Category ID", entity.id, DEBUG)
								.append("Category Key", entity.name, DEBUG)
								.append("Category Name", AndroidTools.getText(context, entity.name))
								.append("Category Image", entity.fallbackImageResourceName, DEBUG)
								.append("Parent ID", entity.parentID, DEBUG)
								.append("Inside", entity.parentName)
								.append("# of direct subcategories", entity.numDirectChildren)
								.append("# of subcategories", entity.numAllChildren)
								.append("# of items in this category", entity.numDirectItems)
								.append("# of items in subcategories", entity.numAllItems)
								.build();
					}
				};

				final CategoryAdapter catAdapter = new CategoryAdapter(new CategoryItemEventsForwarder());
				final GalleryAdapter galAdapter = new GalleryAdapter(null, new GalleryItemEventsForwarder());

				ConcatAdapter adapter = new ConcatAdapter(headerAdapter, catAdapter, galAdapter);
				final int columns = getContext().getResources().getInteger(R.integer.gallery_columns);
				GridLayoutManager layout = new GridLayoutManager(getContext(), columns);
				layout.setSpanSizeLookup(new SpanSizeLookup() {
					@Override public int getSpanSize(int position) {
						return position < headerAdapter.getItemCount() + catAdapter.getItemCount()? columns : 1;
					}
				});

				list.setLayoutManager(layout);
				list.setAdapter(adapter);

				add(Loaders.SingleCategory, headerAdapter);
				add(Loaders.Categories, catAdapter);
				add(Loaders.Items, galAdapter);
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

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.generic_list, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override protected void onStartLoading() {
		Long id = getArgCategoryID();
		// don't show internal header category query doesn't support null so no rows will be returned
		Bundle catArgs = new Bundle();
		catArgs.putSerializable(Extras.CATEGORY_ID, id);

		listController.startLoad(Loaders.SingleCategory, catArgs);
		if (getArgFlatten()) {
			Bundle args = new Bundle(catArgs);
			args.putBoolean(Extras.INCLUDE_SUBS, true);
			listController.startLoad(Loaders.Items, args);
			listController.startLoad(Loaders.Categories, ExtrasFactory.bundleFromCategory(Category.ID_ADD)); // 0 rows
		} else {
			listController.startLoad(Loaders.Items, catArgs);
			listController.startLoad(Loaders.Categories, catArgs);
		}
	}

	private Long getArgCategoryID() {
		return (Long)getArguments().get(Extras.CATEGORY_ID);
	}
	private boolean getArgFlatten() {
		return getArguments().getBoolean(Extras.INCLUDE_SUBS, false);
	}

	@Override protected void onRefresh() {
		listController.refresh();
	}

	public static CategoryFragment newInstance(Long parentCategoryID, boolean flatten) {
		CategoryFragment fragment = new CategoryFragment();
		Bundle args = new Bundle();
		args.putSerializable(Extras.CATEGORY_ID, parentCategoryID);
		args.putBoolean(Extras.INCLUDE_SUBS, flatten);
		fragment.setArguments(args);
		return fragment;
	}

	private class CategoryItemEventsForwarder implements CategoryItemEvents {
		@Override public void showItemsInCategory(long categoryID) {
			getActivity().startActivity(CategoryActivity.showFlattened(categoryID));
		}

		@Override public void onItemClick(int position, long recyclerViewItemID) {
			eventsListener.categorySelected(recyclerViewItemID);
		}

		@Override public boolean onItemLongClick(int position, long recyclerViewItemID) {
			eventsListener.categoryActioned(recyclerViewItemID);
			return true;
		}
	}

	private class GalleryItemEventsForwarder implements GalleryItemEvents {
		@Override public void onItemClick(int position, long recyclerViewItemID) {
			eventsListener.itemSelected(recyclerViewItemID);
		}
		@Override public boolean onItemLongClick(int position, long recyclerViewItemID) {
			eventsListener.itemActioned(recyclerViewItemID);
			return true;
		}
	}

	private class DetailsEventsForwarder implements DetailsEvents {
		@Override public void showImage(String path) {
			try {
				File file = new File(path);
				Uri uri = FileProvider.getUriForFile(getContext(), Constants.AUTHORITY_IMAGES, file);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				if (App.getPrefs().getBoolean(getString(R.string.pref_internalImageViewer), true)) {
					intent.setComponent(new ComponentName(getContext(), ImageActivity.class));
				}
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.setDataAndType(uri, "image/jpeg");
				getActivity().startActivity(intent);
			} catch (Exception ex) {
				LOG.warn("Cannot start image viewer for {}", path, ex);
			}
		}
		@Override public void editImage(long id) {

		}
	}
}
