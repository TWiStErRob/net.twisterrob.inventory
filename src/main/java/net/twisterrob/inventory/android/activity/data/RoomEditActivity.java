package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.RoomEditFragment;

public class RoomEditActivity extends BaseEditActivity<RoomEditFragment> {
	@Override
	protected RoomEditFragment onCreateFragment(Bundle savedInstanceState) {
		return RoomEditFragment.newInstance(getExtraPropertyID(), getExtraRoomID());
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
