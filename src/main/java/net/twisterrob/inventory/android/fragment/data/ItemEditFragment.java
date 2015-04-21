package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import net.twisterrob.android.content.loader.DynamicLoaderManager;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.data.ItemEditFragment.ItemEditEvents;
import net.twisterrob.inventory.android.view.CursorSwapper;
import net.twisterrob.java.utils.ObjectTools;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemEditFragment extends BaseEditFragment<ItemEditEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemEditFragment.class);

	public interface ItemEditEvents {
		void itemLoaded(ItemDTO item);
		void itemSaved(long itemID);
	}

	public ItemEditFragment() {
		setDynamicResource(DYN_EventsClass, ItemEditEvents.class);
	}

	@Override public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);
		name.setHint(R.string.item_name_hint);
		description.setHint(R.string.item_description_hint);
	}

	@Override
	protected void onStartLoading() {
		long id = getArgItemID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper catCursorSwapper = new CursorSwapper(getContext(), typeAdapter);
		Dependency<Cursor> populateCats = manager.add(ItemCategories.id(), null, catCursorSwapper);

		if (id != Item.ID_ADD) {
			Dependency<Cursor> loadItemData = manager.add(SingleItem.id(),
					ExtrasFactory.bundleFromItem(id), new SingleRowLoaded());
			loadItemData.dependsOn(populateCats);
		}

		manager.startLoading();
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		ItemDTO item = ItemDTO.fromCursor(cursor);
		onSingleRowLoaded(item);
		eventsListener.itemLoaded(item);
	}

	@Override
	protected void doSave() {
		new SaveTask().execute(getCurrentItem());
	}

	private ItemDTO getCurrentItem() {
		ItemDTO item = new ItemDTO();
		item.parentID = getArgParentID();
		item.id = getArgItemID();
		item.name = name.getText().toString();
		item.description = description.getText().toString();
		item.tempImageUri = getCurrentImage();
		item.type = type.getSelectedItemId();
		return item;
	}

	private long getArgItemID() {
		return getArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	private long getArgParentID() {
		return getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
	}

	private final class SaveTask extends BaseSaveTask<ItemDTO> {
		@Override protected ItemDTO saveInTransaction(Database db, ItemDTO param) throws Exception {
			if (param.id == Item.ID_ADD) {
				param.id = db.createItem(param.parentID, param.type, param.name, param.description);
			} else {
				db.updateItem(param.id, param.type, param.name, param.description);
			}
			if (!ObjectTools.equals(param.getImageUri(), param.tempImageUri)) {
				db.setItemImage(param.id, param.getImage(getContext()), null);
			}
			return param;
		}

		@Override protected void onResult(ItemDTO result, ItemDTO param) {
			eventsListener.itemSaved(result.id);
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
