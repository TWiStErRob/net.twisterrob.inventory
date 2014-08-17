package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.*;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.view.CursorSwapper;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemEditFragment extends BaseEditFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemEditFragment.class);

	@Override
	protected String getBaseFileName() {
		return "Item_" + getArgItemID();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.item_edit, container, false);
	}

	@Override
	protected void onStartLoading() {
		long id = getArgItemID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper catCursorSwapper = new CursorSwapper(getActivity(), typeAdapter);
		Dependency<Cursor> populateCats = manager.add(ItemCategories.ordinal(), null, catCursorSwapper);

		if (id != Item.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.ITEM_ID, id);
			Dependency<Cursor> loadItemData = manager.add(SingleItem.ordinal(), args, new SingleRowLoaded());

			loadItemData.dependsOn(populateCats);
		} else {
			setTitle(getString(R.string.item_new));
			setCurrentImageDriveId(null, R.drawable.image_add);
		}

		manager.startLoading();
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		ItemDTO item = ItemDTO.fromCursor(cursor);

		setTitle(item.name);
		title.setText(item.name);
		AndroidTools.selectByID(type, item.category);
		setCurrentImageDriveId(item.image, item.getFallbackDrawable(getActivity()));
	}

	@Override
	protected void save() {
		new SaveTask().execute(getCurrentItem());
	}

	private ItemDTO getCurrentItem() {
		ItemDTO item = new ItemDTO();
		item.parentID = getArgParentID();
		item.id = getArgItemID();
		item.name = title.getText().toString();
		item.image = getCurrentImageDriveId();
		item.category = type.getSelectedItemId();
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
					return db.createItem(param.parentID, param.name, param.category, param.image);
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
				App.toast("Item name must be unique within the item collection");
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
