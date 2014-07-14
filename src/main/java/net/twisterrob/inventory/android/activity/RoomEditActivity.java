package net.twisterrob.inventory.android.activity;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.widget.*;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class RoomEditActivity extends BaseEditActivity {
	public static final String EXTRA_ROOM_ID = "roomID";

	private long roomID;

	private static class ViewHolder {
		EditText roomName;
		Spinner roomType;

		void fill(Activity root) {
			roomName = (EditText)root.findViewById(R.id.propertyName);
			roomType = (Spinner)root.findViewById(R.id.propertyType);
		}
	}
	private final ViewHolder view = new ViewHolder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_edit);
		view.fill(this);

		roomID = getIntent().getLongExtra(EXTRA_ROOM_ID, Property.ID_ADD);

		CursorAdapter adapter = Adapters.loadCursorAdapter(this, R.xml.room_types, (Cursor)null);
		getSupportLoaderManager().initLoader(Loaders.RoomTypes.ordinal(), null, new CursorSwapper(this, adapter));

		view.roomType.setAdapter(adapter);
		view.roomType.setOnItemSelectedListener(new DefaultValueUpdater(view.roomName, Room.NAME));
	}
	@Override
	protected Cursor getEditedItem() {
		Cursor room = null;
		if (roomID != Room.ID_ADD) {
			room = App.getInstance().getDataBase().getRoom(roomID);
		}
		return room;
	}

	@Override
	protected void fillEditedItem(Cursor item) {
		String name = item.getString(item.getColumnIndex(Room.NAME));
		long type = item.getLong(item.getColumnIndex(Room.TYPE));

		setTitle(name);
		view.roomName.setText(name);
		AndroidTools.selectByID(view.roomType, type);
	}
}
