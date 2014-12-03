package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.view.MenuItem;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.ItemEditActivity;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.data.ItemViewFragment.ItemEvents;
import net.twisterrob.inventory.android.tasks.DeleteItemTask;
import net.twisterrob.inventory.android.view.Dialogs;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemViewFragment extends BaseViewFragment<ItemDTO, ItemEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemViewFragment.class);

	public interface ItemEvents {
		void itemLoaded(ItemDTO item);
		void itemDeleted(ItemDTO item);
	}

	public ItemViewFragment() {
		setDynamicResource(DYN_EventsClass, ItemEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.item);
	}

	@Override
	protected void onRefresh() {
		super.onRefresh();
		getLoaderManager().getLoader(SingleItem.ordinal()).onContentChanged();
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		getLoaderManager().initLoader(SingleItem.ordinal(),
				ExtrasFactory.bundleFromItem(getArgItemID()), new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		ItemDTO item = ItemDTO.fromCursor(cursor);
		super.onSingleRowLoaded(item);
		eventsListener.itemLoaded(item);
	}

	@Override
	protected CharSequence getDetailsString(ItemDTO entity) {
		return new DescriptionBuilder()
				.append("Item ID", entity.id, BuildConfig.DEBUG)
				.append("Item Name", entity.name)
				.append("Parent ID", entity.parentID, BuildConfig.DEBUG)
				.append("Category ID", entity.category, BuildConfig.DEBUG)
				.append("Category Name", entity.categoryName, BuildConfig.DEBUG)
				.append("Category", AndroidTools.getText(getContext(), entity.categoryName))
				.append("Inside", entity.parentName != null? entity.parentName : "the room")
				.append("Room ID", entity.room, BuildConfig.DEBUG)
				.append("Room", entity.roomName)
				.append("Room Root", entity.roomRoot)
				.append("Property ID", entity.property, BuildConfig.DEBUG)
				.append("Property", entity.propertyName)
				.append("# of items in this item", entity.numDirectItems)
				.append("# of items inside", entity.numAllItems)
				.append("image", entity.image, BuildConfig.DEBUG)
				.build();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_edit:
				startActivity(ItemEditActivity.edit(getArgItemID()));
				return true;
			case R.id.action_item_delete:
				delete(getArgItemID());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void delete(final long itemID) {
		new DeleteItemTask(itemID, new Dialogs.Callback() {
			public void dialogSuccess() {
				ItemDTO item = new ItemDTO();
				item.id = itemID;
				eventsListener.itemDeleted(item);
			}

			public void dialogFailed() {
				App.toast("Cannot delete item #" + itemID);
			}
		}).displayDialog(getActivity());
	}

	private long getArgItemID() {
		return getArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	public static ItemViewFragment newInstance(long itemID) {
		if (itemID == Item.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing item");
		}

		ItemViewFragment fragment = new ItemViewFragment();
		fragment.setArguments(ExtrasFactory.bundleFromItem(itemID));
		return fragment;
	}
}
