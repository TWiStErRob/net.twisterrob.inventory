package net.twisterrob.inventory.android.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
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

public class RoomEditActivity extends BaseActivity {
	private EditText roomName;
	private Spinner roomType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		super.setContentView(R.layout.property_edit);
		roomName = (EditText)findViewById(R.id.propertyName);
		roomType = (Spinner)findViewById(R.id.propertyType);

		CursorAdapter adapter = Adapters.loadCursorAdapter(this, R.xml.room_types, (Cursor)null);
		roomType.setAdapter(adapter);
		roomType.setOnItemSelectedListener(new DefaultValueUpdater(roomName, Room.NAME));

		DynamicLoaderManager manager = new DynamicLoaderManager(getSupportLoaderManager());
		Bundle args = getIntent().getExtras();
		Dependency<Cursor> loadRoomData = manager.add(SingleRoom.ordinal(), args, new LoadExistingRoom());
		Dependency<Void> loadRoomCondition = manager.add(-SingleRoom.ordinal(), args, new IsExistingRoom());
		Dependency<Cursor> populateTypes = manager.add(RoomTypes.ordinal(), null, new CursorSwapper(this, adapter));

		populateTypes.providesResultFor(loadRoomData.dependsOn(loadRoomCondition));
		manager.startLoading();
	}

	private final class IsExistingRoom extends DynamicLoaderManager.Condition {
		private IsExistingRoom() {
			super(RoomEditActivity.this);
		}

		@Override
		protected boolean test(int id, Bundle args) {
			return args != null && args.getLong(Extras.ROOM_ID, Room.ID_ADD) != Room.ID_ADD;
		}
	}

	private final class LoadExistingRoom extends LoadSingleRow {
		private LoadExistingRoom() {
			super(RoomEditActivity.this);
		}

		@Override
		protected void process(Cursor item) {
			super.process(item);
			String name = item.getString(item.getColumnIndex(Room.NAME));
			long type = item.getLong(item.getColumnIndex(Room.TYPE));

			setTitle(name);
			roomName.setText(name);
			AndroidTools.selectByID(roomType, type);
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			finish();
		}
	}
}
