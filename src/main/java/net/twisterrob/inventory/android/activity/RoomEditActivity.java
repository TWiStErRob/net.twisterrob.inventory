package net.twisterrob.inventory.android.activity;

import android.database.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class RoomEditActivity extends FragmentActivity {
	public static final String EXTRA_ROOM_ID = "roomID";

	private long roomID;
	private int preselectedRoomType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_edit);
		final EditText roomName = (EditText)findViewById(R.id.propertyName);
		final Spinner roomType = (Spinner)findViewById(R.id.propertyType);

		roomID = getIntent().getLongExtra(EXTRA_ROOM_ID, Property.ID_ADD);

		CursorAdapter propertiesTypeAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
				new String[]{RoomType.NAME}, new int[]{android.R.id.text1}, 0);
		getSupportLoaderManager().initLoader(Loaders.RoomTypes.ordinal(), null,
				new CursorSwapper(this, propertiesTypeAdapter) {
					@Override
					public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
						super.onLoadFinished(loader, data);
						AndroidTools.selectByID(roomType, preselectedRoomType);
					}
				});

		roomType.setAdapter(propertiesTypeAdapter);

		if (roomID != Property.ID_ADD) {
			Cursor room = App.getInstance().getDataBase().getProperty(roomID);
			DatabaseUtils.dumpCursor(room);
			if (room.getCount() == 1) {
				room.moveToFirst();
				roomName.setText(room.getString(room.getColumnIndex(Property.NAME)));
				preselectedRoomType = (int)room.getLong(room.getColumnIndex(Property.TYPE));
			} else {
				String msg = "Room #" + roomID + " not found!";
				Toast.makeText(RoomEditActivity.this, msg, Toast.LENGTH_LONG).show();
			}
			room.close();
		}
	}
}
