package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.widget.*;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.LoadSingleRow;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.view.*;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class RoomEditFragment extends BaseEditFragment {
	private EditText roomName;
	private Spinner roomType;
	private CursorAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.room_edit, container, false);
		roomName = (EditText)root.findViewById(R.id.roomName);
		roomType = (Spinner)root.findViewById(R.id.roomType);

		adapter = Adapters.loadCursorAdapter(getActivity(), R.xml.room_types, (Cursor)null);
		roomType.setAdapter(adapter);
		roomType.setOnItemSelectedListener(new DefaultValueUpdater(roomName, Room.NAME));

		return root;
	}

	@Override
	public void load(long id) {
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

	@Override
	public void save() {}

	private final class LoadExistingRoom extends LoadSingleRow {
		private LoadExistingRoom() {
			super(getActivity());
		}

		@Override
		protected void process(Cursor item) {
			super.process(item);
			String name = item.getString(item.getColumnIndexOrThrow(Room.NAME));
			long type = item.getLong(item.getColumnIndexOrThrow(Room.TYPE));

			getActivity().setTitle(name);
			AndroidTools.selectByID(roomType, type);
			roomName.setText(name); // must set it after roomType to prevent auto-propagation
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
	}
}
