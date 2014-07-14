package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class RoomsActivity extends BaseListActivity {
	private static final Logger LOG = LoggerFactory.getLogger(RoomsActivity.class);

	public static final String EXTRA_PROPERTY_ID = "propertyID";

	private long propertyID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_list);
		propertyID = getIntent().getLongExtra(EXTRA_PROPERTY_ID, Property.ID_ADD);

		CursorAdapter propertiesAdapter = new SimpleCursorAdapter(this, R.layout.property_item, null, new String[]{
				Room.NAME, "image"}, new int[]{R.id.propertyName, R.id.propertyImage}, 0);
		Bundle args = new Bundle();
		args.putLong(EXTRA_PROPERTY_ID, propertyID);
		getSupportLoaderManager().initLoader(Loaders.Rooms.ordinal(), args, new CursorSwapper(this, propertiesAdapter));

		GridView rooms = (GridView)findViewById(R.id.properties);
		rooms.setAdapter(propertiesAdapter);

		rooms.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				Intent intent = new Intent(getApplicationContext(), RoomEditActivity.class);
				intent.putExtra(RoomEditActivity.EXTRA_ROOM_ID, id);
				startActivity(intent);
				return true;
			}
		});
		rooms.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				if (id == Room.ID_ADD) {
					Intent intent = new Intent(getApplicationContext(), RoomEditActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(RoomsActivity.this, "Not implemented", Toast.LENGTH_LONG).show();
					//Intent intent = new Intent(getApplicationContext(), ItemsActivity.class);
					//intent.putExtra(ItemsActivity.EXTRA_ROOM_ID, id);
					//startActivity(intent);
				}
			}
		});
	}
}
