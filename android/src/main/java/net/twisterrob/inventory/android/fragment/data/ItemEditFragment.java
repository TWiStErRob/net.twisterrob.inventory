package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;

import net.twisterrob.android.content.loader.DynamicLoaderManager;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Item;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.data.ItemEditFragment.ItemEditEvents;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemEditFragment extends BaseEditFragment<ItemEditEvents, ItemDTO> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemEditFragment.class);

	public interface ItemEditEvents {
		void itemLoaded(ItemDTO item);
		void itemSaved(long itemID);
	}

	public ItemEditFragment() {
		setDynamicResource(DYN_EventsClass, ItemEditEvents.class);
		setDynamicResource(DYN_NameHintResource, R.string.item_name_hint);
		setDynamicResource(DYN_DescriptionHintResource, R.string.item_description_hint);
	}

	@Override protected void onStartLoading() {
		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		Dependency<Cursor> populateCats = manager.add(ItemCategoriesAll.id(), null, getTypeCallback());

		if (!isNew()) {
			Dependency<Cursor> loadItemData = manager.add(SingleItem.id(),
					Intents.bundleFromItem(getArgItemID()),
					SingleItem.createCallbacks(requireContext(), new SingleRowLoaded())
			);
			loadItemData.dependsOn(populateCats);
		}

		manager.startLoading();
	}

	@Override protected void onSingleRowLoaded(@NonNull Cursor cursor) {
		ItemDTO item = ItemDTO.fromCursor(cursor);
		onSingleRowLoaded(item);
		eventsListener.itemLoaded(item);
	}

	@Override protected @NonNull ItemDTO createDTO() {
		ItemDTO item = new ItemDTO();
		item.parentID = getArgParentID();
		item.id = getArgItemID();
		return item;
	}

	@Override protected boolean isNew() {
		return getArgItemID() == Item.ID_ADD;
	}

	private long getArgItemID() {
		return requireArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	private long getArgParentID() {
		return requireArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
	}

	@Override protected ItemDTO onSave(Database db, ItemDTO param) {
		if (param.id == Item.ID_ADD) {
			param.id = db.createItem(param.parentID, param.type, param.name, param.description);
		} else {
			db.updateItem(param.id, param.type, param.name, param.description);
		}
		if (!param.hasImage) {
			// may clear already cleared images, but there's not enough info
			db.setItemImage(param.id, null);
		} else if (param.image != null) {
			db.setItemImage(param.id, db.addImage(param.image, null));
		} else {
			// it has an image, but there's no blob -> the image is already in DB
		}
		return param;
	}

	@Override protected void onSaved(@NonNull ItemDTO result) {
		// FIXME this can be called when it's null if Save-rotate or Save-home is done quick enough
		eventsListener.itemSaved(result.id);
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
