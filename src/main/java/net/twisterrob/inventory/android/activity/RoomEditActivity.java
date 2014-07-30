package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.RoomEditFragment;

public class RoomEditActivity extends BaseEditActivity {
	private RoomEditFragment editor;
	private long currentRoomID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.room);

		currentRoomID = getIntent().getLongExtra(Extras.ROOM_ID, Room.ID_ADD);
		if (currentRoomID == Room.ID_ADD) {
			Toast.makeText(this, "Invalid room ID", Toast.LENGTH_LONG).show();
			finish();
		}
		editor = getFragment(R.id.room);
	}

	@Override
	protected void onStart() {
		super.onStart();
		editor.load(currentRoomID);
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
