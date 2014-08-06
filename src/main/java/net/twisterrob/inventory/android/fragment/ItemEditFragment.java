package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.view.*;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemEditFragment extends BaseEditFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemEditFragment.class);

	private EditText itemName;
	private Spinner itemCategory;
	private CursorAdapter adapter;

	public ItemEditFragment() {
		setDynamicResource(DYN_ImageView, R.id.itemImage);
	}

	@Override
	protected String getBaseFileName() {
		return "Item_" + getArgItemID();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.item_edit, container, false);
		itemName = (EditText)root.findViewById(R.id.itemName);
		itemCategory = (Spinner)root.findViewById(R.id.itemCategory);

		((Button)root.findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		adapter = new ItemCategoryAdapter(getActivity());
		itemCategory.setAdapter(adapter);

		return root;
	}

	@Override
	protected void onStartLoading() {
		long id = getArgItemID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper catCursorSwapper = new CursorSwapper(getActivity(), adapter);
		Dependency<Cursor> populateCats = manager.add(ItemCategories.ordinal(), null, catCursorSwapper);

		if (id != Item.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.ITEM_ID, id);
			Dependency<Cursor> loadItemData = manager.add(SingleItem.ordinal(), args, new SingleRowLoaded());

			loadItemData.dependsOn(populateCats);
		} else {
			setCurrentImageDriveId(null, R.drawable.image_add);
		}

		manager.startLoading();
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		ItemDTO item = ItemDTO.fromCursor(cursor);

		getActivity().setTitle(item.name);
		itemName.setText(item.name);
		AndroidTools.selectByID(itemCategory, item.category);
		setCurrentImageDriveId(item.image, item.getFallbackDrawable(getActivity()));
	}

	private void save() {
		new SaveTask().execute(getCurrentItem());
	}

	private ItemDTO getCurrentItem() {
		ItemDTO item = new ItemDTO();
		item.parentID = getArgParentID();
		item.id = getArgItemID();
		item.name = itemName.getText().toString();
		item.image = getCurrentImageDriveId();
		item.category = itemCategory.getSelectedItemId();
		return item;
	}

	private long getArgItemID() {
		return getArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	private long getArgParentID() {
		return getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
	}

	private final class SaveTask extends SimpleAsyncTask<ItemDTO, Void, Long> {
		@Override
		protected Long doInBackground(ItemDTO param) {
			try {
				Database db = App.db();
				if (param.id == Item.ID_ADD) {
					return db.newItem(param.parentID, param.name, param.category, param.image);
				} else {
					db.updateItem(param.id, param.name, param.category, param.image);
					return param.id;
				}
			} catch (SQLiteConstraintException ex) {
				LOG.warn("Cannot save {}", param, ex);
				return null;
			}
		}

		@Override
		protected void onPostExecute(Long result) {
			if (result != null) {
				getActivity().finish();
			} else {
				Toast.makeText(getActivity(), "Item name must be unique within the item collection", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	public static ItemEditFragment newInstance(long parentID, long itemID) {
		if (parentID == Item.ID_ADD && itemID == Item.ID_ADD) {
			throw new IllegalArgumentException("Parent item ID / item ID must be provided (new item / edit item)");
		}
		if (itemID != Item.ID_ADD) { // no need to know which parent when editing
			parentID = Item.ID_ADD;
		}

		ItemEditFragment fragment = new ItemEditFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, parentID);
		args.putLong(Extras.ITEM_ID, itemID);

		fragment.setArguments(args);
		return fragment;
	}
}
