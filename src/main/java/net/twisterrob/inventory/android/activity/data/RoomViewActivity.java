package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.fragment.data.RoomViewFragment.RoomEvents;

public class RoomViewActivity extends BaseDetailActivity<ItemListFragment> implements RoomEvents, ItemsEvents {
	private RoomDTO current;

	@Override
	protected ItemListFragment onCreateFragment(Bundle savedInstanceState) {
		return ItemListFragment.newRoomInstance(getExtraRoomID()).addHeader();
	}

	public void roomLoaded(RoomDTO room) {
		setActionBarTitle(room.name);
		current = room;
	}
	public void roomDeleted(RoomDTO room) {
		finish();
	}

	public void newItem(long parentID) {
		startActivity(ItemEditActivity.add(parentID));
	}
	public void itemSelected(long id) {
		startActivity(ItemViewActivity.show(id));
		// TODO consider tabs as breadcrumbs?
	}
	public void itemActioned(long id) {
		startActivity(ItemEditActivity.edit(id));
	}

	@Override
	protected String checkExtras() {
		if (getExtraRoomID() == Room.ID_ADD) {
			return "Invalid room ID";
		}
		return null;
	}

	private long getExtraRoomID() {
		return getIntent().getLongExtra(Extras.ROOM_ID, Item.ID_ADD);
	}

	@Override public Intent getSupportParentActivityIntent() {
		Intent intent = super.getSupportParentActivityIntent();
		intent.putExtra(Extras.PROPERTY_ID, current != null? current.propertyID : Property.ID_ADD);
		return intent;
	}

	public static Intent show(long roomID) {
		if (roomID == Room.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing room");
		}

		Intent intent = new Intent(App.getAppContext(), RoomViewActivity.class);
		intent.putExtra(Extras.ROOM_ID, roomID);
		return intent;
	}
}
