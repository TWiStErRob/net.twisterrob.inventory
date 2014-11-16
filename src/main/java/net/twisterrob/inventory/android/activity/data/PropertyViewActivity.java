package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.PropertyViewFragment.PropertyEvents;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;

public class PropertyViewActivity extends BaseDetailActivity<PropertyViewFragment, RoomListFragment>
		implements PropertyEvents, RoomsEvents {
	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		long propertyID = getExtraPropertyID();
		PropertyViewFragment detailsFragment;
		if (propertyID == Property.ID_ADD) {
			setActionBarSubtitle(null);
			setActionBarTitle(getText(R.string.room_all));
			setIcon(R.raw.room_unknown);
			hideDetails();
			detailsFragment = null;
		} else {
			setIcon(R.raw.property_unknown);
			detailsFragment = PropertyViewFragment.newInstance(propertyID);
		}
		setFragments(detailsFragment, RoomListFragment.newInstance(propertyID));
	}

	public void propertyLoaded(PropertyDTO property) {
		// ignore
	}

	public void propertyDeleted(PropertyDTO property) {
		finish();
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

	public static Intent show(long propertyID) {
		Intent intent = new Intent(App.getAppContext(), PropertyViewActivity.class);
		intent.putExtra(Extras.PROPERTY_ID, propertyID);
		return intent;
	}
}
