package net.twisterrob.inventory.android.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.widget.*;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class RoomEditActivity extends BaseActivity {
	public static final String EXTRA_ROOM_ID = "roomID";

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

		Bundle args = getIntent().getExtras();
		InitAction<Cursor> roomTypes = new InitAction<Cursor>(getSupportLoaderManager(), Loaders.RoomTypes.ordinal(),
				null, new CursorSwapper(this, adapter));
		InitAction<Cursor> singleRoom = new InitAction<Cursor>(getSupportLoaderManager(), Loaders.SingleRoom.ordinal(),
				args, new LoadRoom()) {
			@Override
			public void run() {
				if (getArgs().getLong(EXTRA_ROOM_ID, Room.ID_ADD) != Room.ID_ADD) {
					super.run();
				}
			}
		};

		LoaderChain.sequence(roomTypes, singleRoom).run();
	}

	private class LoadRoom extends LoadSingleRow {
		LoadRoom() {
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
