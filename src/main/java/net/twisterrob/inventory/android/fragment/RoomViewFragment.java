package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.fragment.RoomViewFragment.RoomEvents;
import net.twisterrob.inventory.android.tasks.DeleteRoomTask;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class RoomViewFragment extends BaseViewFragment<RoomEvents> {
	public interface RoomEvents {
		void roomLoaded(RoomDTO room);
		void roomDeleted(RoomDTO room);
	}

	public RoomViewFragment() {
		setDynamicResource(DYN_EventsClass, RoomEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.room);
	}

	@Override
	protected void onRefresh() {
		getLoaderManager().getLoader(SingleRoom.ordinal()).forceLoad();
	}

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.ROOM_ID, getArgRoomID());
		getLoaderManager().initLoader(SingleRoom.ordinal(), args, new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		RoomDTO room = RoomDTO.fromCursor(cursor);

		getActivity().setTitle(room.name);
		title.setText(room.name);
		type.setText(String.valueOf(room.type));
		App.pic().load(room.image).placeholder(room.getFallbackDrawableID(getActivity())).into(image);

		eventsListener.roomLoaded(room);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_room_edit:
				startActivity(RoomEditActivity.edit(getArgRoomID()));
				return true;
			case R.id.action_room_delete:
				delete(getArgRoomID());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void delete(final long roomID) {
		new DeleteRoomTask(roomID, new Dialogs.Callback() {
			public void dialogSuccess() {
				RoomDTO room = new RoomDTO();
				room.id = roomID;
				eventsListener.roomDeleted(room);
			}

			public void dialogFailed() {
				Toast.makeText(getActivity(), "Cannot delete room #" + roomID, Toast.LENGTH_LONG).show();
			}
		}).displayDialog(getActivity());
	}

	private long getArgRoomID() {
		return getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
	}

	public static RoomViewFragment newInstance(long roomID) {
		if (roomID == Room.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing room");
		}

		RoomViewFragment fragment = new RoomViewFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.ROOM_ID, roomID);

		fragment.setArguments(args);
		return fragment;
	}
}
