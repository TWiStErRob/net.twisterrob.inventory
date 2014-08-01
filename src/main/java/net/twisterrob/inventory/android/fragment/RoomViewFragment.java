package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.LoadSingleRow;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.fragment.RoomViewFragment.RoomEvents;
import net.twisterrob.inventory.android.tasks.DeleteRoomTask;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class RoomViewFragment extends BaseViewFragment<RoomEvents> {
	public interface RoomEvents {
		void roomLoaded(RoomDTO room);
	}

	public RoomViewFragment() {
		setDynamicResource(DYN_EventsClass, RoomEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.room);
	}

	private TextView roomName;
	private TextView roomType;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.room_view, container, false);
		roomName = (TextView)root.findViewById(R.id.roomName);
		roomType = (TextView)root.findViewById(R.id.roomType);
		return root;
	}

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.ROOM_ID, getArgRoomID());
		getLoaderManager().initLoader(SingleRoom.ordinal(), args, new LoadExistingRoom());
	}

	public void refresh() {
		getLoaderManager().getLoader(SingleRoom.ordinal()).forceLoad();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_room_edit:
				startActivity(RoomEditActivity.edit(getArgRoomID()));
				return true;
			case R.id.action_room_delete:
				Dialogs.executeTask(getActivity(), new DeleteRoomTask(getArgRoomID(), new Dialogs.Callback() {
					public void success() {
						getActivity().finish();
					}
					public void failed() {
						String message = "This property still has some items";
						Toast.makeText(App.getAppContext(), message, Toast.LENGTH_LONG).show();
					}
				}));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private long getArgRoomID() {
		return getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
	}

	private final class LoadExistingRoom extends LoadSingleRow {
		private LoadExistingRoom() {
			super(getActivity());
		}

		@Override
		protected void process(Cursor cursor) {
			super.process(cursor);
			RoomDTO room = RoomDTO.fromCursor(cursor);

			getActivity().setTitle(room.name);
			roomName.setText(room.name);
			roomType.setText(String.valueOf(room.type));

			eventsListener.roomLoaded(room);
		}
		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
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
