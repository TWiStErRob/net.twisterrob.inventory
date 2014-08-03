package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.PropertyViewFragment.PropertyEvents;
import net.twisterrob.inventory.android.fragment.RoomListFragment.RoomsEvents;

public class PropertyViewActivity extends BaseDetailActivity<PropertyViewFragment, RoomListFragment>
		implements
			PropertyEvents,
			RoomsEvents {

	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		long propertyID = getExtraPropertyID();
		setFragments(PropertyViewFragment.newInstance(propertyID), RoomListFragment.newInstance(propertyID));
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

	@Override
	protected String checkExtras() {
		if (getExtraPropertyID() == Property.ID_ADD) {
			return "Invalid property ID";
		}
		return null;
	}

	private long getExtraPropertyID() {
		return getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static Intent show(long propertyID) {
		Intent intent = new Intent(App.getAppContext(), PropertyViewActivity.class);
		intent.putExtra(Extras.PROPERTY_ID, propertyID);
		return intent;
	}
}
