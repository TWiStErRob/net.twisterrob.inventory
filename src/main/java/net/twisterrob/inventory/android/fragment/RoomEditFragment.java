package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.view.*;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class RoomEditFragment extends BaseEditFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomEditFragment.class);

	private EditText roomName;
	private Spinner roomType;
	private CursorAdapter adapter;

	@Override
	protected String getBaseFileName() {
		return "Room_" + getArgRoomID();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.room_edit, container, false);
		roomName = (EditText)root.findViewById(R.id.roomName);
		roomType = (Spinner)root.findViewById(R.id.roomType);
		((Button)root.findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		adapter = Adapters.loadCursorAdapter(getActivity(), R.xml.room_types, (Cursor)null);
		roomType.setAdapter(adapter);
		roomType.setOnItemSelectedListener(new DefaultValueUpdater(roomName, Room.NAME));

		return root;
	}

	@Override
	protected void onStartLoading() {
		long id = getArgRoomID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper typeCursorSwapper = new CursorSwapper(getActivity(), adapter);
		Dependency<Cursor> populateTypes = manager.add(RoomTypes.ordinal(), null, typeCursorSwapper);

		if (id != Room.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.ROOM_ID, id);
			Dependency<Cursor> loadRoomData = manager.add(SingleRoom.ordinal(), args, new LoadExistingRoom());

			loadRoomData.dependsOn(populateTypes); // type is auto-selected when a room is loaded
		}

		manager.startLoading();
	}

	private void save() {
		new SaveTask().execute(getCurrentRoom());
	}

	private RoomDTO getCurrentRoom() {
		RoomDTO room = new RoomDTO();
		room.propertyID = getArgPropertyID();
		room.id = getArgRoomID();
		room.name = roomName.getText().toString();
		room.type = roomType.getSelectedItemId();
		return room;
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
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
			AndroidTools.selectByID(roomType, room.type);
			roomName.setText(room.name); // must set it after roomType to prevent auto-propagation
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
	}

	private final class SaveTask extends SimpleAsyncTask<RoomDTO, Void, Long> {
		@Override
		protected Long doInBackground(RoomDTO param) {
			try {
				Database db = App.getInstance().getDataBase();
				if (param.id == Room.ID_ADD) {
					return db.newRoom(param.propertyID, param.name, param.type);
				} else {
					db.updateRoom(param.id, param.name, param.type);
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
				Toast.makeText(getActivity(), "Room name must be unique within the property", Toast.LENGTH_LONG).show();
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
