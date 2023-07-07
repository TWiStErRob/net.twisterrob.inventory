package net.twisterrob.inventory.android.activity.data;

import android.annotation.SuppressLint;
import android.content.Intent;

import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.fragment.data.RoomViewFragment.RoomEvents;

@AndroidEntryPoint
public class RoomViewActivity extends BaseDetailActivity<ItemListFragment> implements RoomEvents, ItemsEvents {
	private RoomDTO current;

	public RoomViewActivity() {
		super(R.plurals.room);
	}

	@Override protected ItemListFragment onCreateFragment() {
		return ItemListFragment.newRoomInstance(getExtraRoomID()).addHeader(null);
	}

	public void roomLoaded(RoomDTO room) {
		setActionBarTitle(room.name);
		current = room;
	}
	@Override public void roomMoved(long roomID, long newPropertyID) {
		startActivity(PropertyViewActivity.show(newPropertyID));
		finish();
	}
	public void roomDeleted(RoomDTO room) {
		finish();
	}

	public void newItem(long parentID) {
		startActivity(ItemEditActivity.add(parentID));
	}
	public void itemSelected(long id) {
		startActivity(Intents.childNav(ItemViewActivity.show(id)));
		// CONSIDER tabs as breadcrumbs?
	}
	public void itemActioned(long id) {
		startActivity(ItemEditActivity.edit(id));
	}

	@Override protected String checkExtras() {
		if (getExtraRoomID() == Room.ID_ADD) {
			return "Invalid room ID";
		}
		return null;
	}

	@Override public void onContentChanged() {
		super.onContentChanged();
		setupTitleEditor();
	}

	@SuppressLint({"WrongThread", "WrongThreadInterprocedural"}) // FIXME DB on UI
	@Override protected void updateName(String newName) {
		App.db().updateRoom(current.id, current.type, newName, current.description);
	}

	private long getExtraRoomID() {
		return getIntent().getLongExtra(Extras.ROOM_ID, Item.ID_ADD);
	}

	@Override public Intent getSupportParentActivityIntent() {
		Intent intent = super.getSupportParentActivityIntent();
		if (intent != null) { // shouldn't be since it's declared in AndroidManifest.xml
			intent.putExtra(Extras.PROPERTY_ID, current != null? current.propertyID : Property.ID_ADD);
		}
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
