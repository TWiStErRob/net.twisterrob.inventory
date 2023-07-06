package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.fragment.data.RoomEditFragment;

@AndroidEntryPoint
public class RoomEditActivity extends BaseEditActivity<RoomEditFragment>
		implements RoomEditFragment.RoomEditEvents {
	@Override protected RoomEditFragment onCreateFragment() {
		return RoomEditFragment.newInstance(getExtraPropertyID(), getExtraRoomID());
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getExtraRoomID() == Room.ID_ADD) {
			setActionBarTitle(getString(R.string.room_new));
		}
	}

	@Override public void roomLoaded(RoomDTO room) {
		//setActionBarTitle(room.name); // don't set
	}

	@Override public void roomSaved(long roomID) {
		Intent data = Intents.intentFromRoom(roomID);
		data.putExtra(Extras.PROPERTY_ID, getExtraPropertyID());
		setResult(RESULT_OK, data);
		finish();
	}

	private long getExtraRoomID() {
		return getIntent().getLongExtra(Extras.ROOM_ID, Room.ID_ADD);
	}

	private long getExtraPropertyID() {
		return getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static Intent add(long propertyID) {
		Intent intent = new Intent(App.getAppContext(), RoomEditActivity.class);
		intent.putExtra(Extras.PROPERTY_ID, propertyID);
		return intent;
	}

	public static Intent edit(long roomID) {
		Intent intent = new Intent(App.getAppContext(), RoomEditActivity.class);
		intent.putExtra(Extras.ROOM_ID, roomID);
		return intent;
	}
}
