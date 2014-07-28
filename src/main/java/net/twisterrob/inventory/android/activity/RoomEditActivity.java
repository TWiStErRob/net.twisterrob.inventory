package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.RoomEditFragment;

public class RoomEditActivity extends BaseEditActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.room);

		RoomEditFragment editor = getFragment(R.id.room);
		editor.edit(getIntent().getLongExtra(Extras.ROOM_ID, Room.ID_ADD));
	}

	public static Intent add() {
		Intent intent = new Intent(App.getAppContext(), RoomEditActivity.class);
		return intent;
	}
	public static Intent edit(long roomId) {
		Intent intent = new Intent(App.getAppContext(), RoomEditActivity.class);
		intent.putExtra(Extras.ROOM_ID, roomId);
		return intent;
	}
}
