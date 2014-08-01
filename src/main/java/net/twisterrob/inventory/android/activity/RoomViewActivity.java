package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.fragment.RoomViewFragment.RoomEvents;

public class RoomViewActivity extends BaseListActivity implements ItemsEvents, RoomEvents {
	private RoomViewFragment room;
	private ItemListFragment items;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.room_view_activity);

		long currentRoomID = getExtraRoomID();
		if (currentRoomID == Room.ID_ADD) {
			Toast.makeText(this, "Invalid room ID", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		room = RoomViewFragment.newInstance(currentRoomID);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.room, room);
		ft.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		room.refresh();
		if (items != null) {
			items.refresh();
		}
	}

	public void roomLoaded(RoomDTO room) {
		if (items == null) {
			items = ItemListFragment.newInstance(room.rootItemID);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.items, items);
			ft.commitAllowingStateLoss();
		}
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

	private long getExtraRoomID() {
		return getIntent().getLongExtra(Extras.ROOM_ID, Item.ID_ADD);
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
