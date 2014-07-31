package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.RoomEditFragment;

public class RoomEditActivity extends BaseEditActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.room_edit_activity);

		long currentPropertyID = getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
		long currentRoomID = getIntent().getLongExtra(Extras.ROOM_ID, Room.ID_ADD);
		Fragment editor = RoomEditFragment.newInstance(currentPropertyID, currentRoomID);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.room, editor);
		ft.commit();
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
