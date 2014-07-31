package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.PropertyViewFragment.PropertyEvents;
import net.twisterrob.inventory.android.fragment.RoomListFragment.RoomsEvents;

public class PropertyViewActivity extends BaseListActivity implements RoomsEvents, PropertyEvents {
	private PropertyViewFragment property;
	private RoomListFragment rooms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_view_activity);

		long currentPropertyID = getExtraPropertyID();
		if (currentPropertyID == Property.ID_ADD) {
			Toast.makeText(this, "Invalid property ID", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		property = PropertyViewFragment.newInstance(currentPropertyID);
		rooms = RoomListFragment.newInstance(currentPropertyID);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.property, property);
		ft.replace(R.id.rooms, rooms);
		ft.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		property.refresh();
		rooms.refresh();
	}

	public void propertyLoaded(PropertyDTO property) {
		// ignore
	}

	public void newRoom(long propertyID) {
		startActivity(RoomEditActivity.add(propertyID));
	}

	public void roomSelected(RoomDTO room) {
		startActivity(RoomViewActivity.show(room.id));
	}

	public void roomActioned(long id) {
		startActivity(RoomEditActivity.edit(id));
	}

	private long getExtraPropertyID() {
		return getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static Intent list(long propertyID) {
		Intent intent = new Intent(App.getAppContext(), PropertyViewActivity.class);
		intent.putExtra(Extras.PROPERTY_ID, propertyID);
		return intent;
	}
}
