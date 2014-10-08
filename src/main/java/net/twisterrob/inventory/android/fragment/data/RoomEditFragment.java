package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.*;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.view.CursorSwapper;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class RoomEditFragment extends BaseEditFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomEditFragment.class);

	@Override
	protected String getBaseFileName() {
		return "Room_" + getArgRoomID();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setKeepNameInSync(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.room_edit, container, false);
	}

	@Override
	protected void onStartLoading() {
		long id = getArgRoomID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper typeCursorSwapper = new CursorSwapper(getActivity(), typeAdapter);
		Dependency<Cursor> populateTypes = manager.add(RoomTypes.ordinal(), null, typeCursorSwapper);

		if (id != Room.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.ROOM_ID, id);
			Dependency<Cursor> loadRoomData = manager.add(SingleRoom.ordinal(), args, new SingleRowLoaded());

			loadRoomData.dependsOn(populateTypes); // type is auto-selected when a room is loaded
		} else {
			setTitle(getString(R.string.room_new));
			setCurrentImageDriveId(null, R.drawable.image_add);
		}

		manager.startLoading();
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		RoomDTO room = RoomDTO.fromCursor(cursor);

		setTitle(room.name);
		AndroidTools.selectByID(type, room.type);
		title.setText(room.name); // must set it after roomType to prevent auto-propagation
		setCurrentImageDriveId(room.image, room.getFallbackDrawable(getActivity()));
	}

	@Override
	protected void save() {
		new SaveTask().execute(getCurrentRoom());
	}

	private RoomDTO getCurrentRoom() {
		RoomDTO room = new RoomDTO();
		room.propertyID = getArgPropertyID();
		room.id = getArgRoomID();
		room.name = title.getText().toString();
		room.type = type.getSelectedItemId();
		room.image = getCurrentImageDriveId();
		return room;
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	private long getArgRoomID() {
		return getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
	}

	private final class SaveTask extends SimpleAsyncTask<RoomDTO, Void, Long> {
		@Override
		protected Long doInBackground(RoomDTO param) {
			try {
				Database db = App.db();
				if (param.id == Room.ID_ADD) {
					return db.createRoom(param.propertyID, param.name, param.type, param.image);
				} else {
					db.updateRoom(param.id, param.name, param.type, param.image);
					return param.id;
				}
			} catch (SQLiteConstraintException ex) {
				LOG.warn("Cannot save {}", param, ex);
				return null;
			}
		}

		@Override
		protected void onPostExecute(Long result) {
			if (result != null) {
				getActivity().finish();
			} else {
				App.toast("Room name must be unique within the property");
			}
		}
	}

	public static RoomEditFragment newInstance(long propertyID, long roomID) {
		if (propertyID == Property.ID_ADD && roomID == Room.ID_ADD) {
			throw new IllegalArgumentException("Property ID / room ID must be provided (new room / edit room)");
		}
		if (roomID != Room.ID_ADD) { // no need to know which property when editing
			propertyID = Property.ID_ADD;
		}

		RoomEditFragment fragment = new RoomEditFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, propertyID);
		args.putLong(Extras.ROOM_ID, roomID);

		fragment.setArguments(args);
		return fragment;
	}
}
