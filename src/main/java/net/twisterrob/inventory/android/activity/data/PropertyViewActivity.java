package net.twisterrob.inventory.android.activity.data;

import org.slf4j.*;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.PropertyViewFragment.PropertyEvents;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;

public class PropertyViewActivity extends BaseDetailActivity<RoomListFragment> implements PropertyEvents, RoomsEvents {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyViewActivity.class);

	@Override protected void onCreate(Bundle savedInstanceState) {
		wantDrawer = getExtraPropertyID() == Property.ID_ADD;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected RoomListFragment onCreateFragment(Bundle savedInstanceState) {
		long propertyID = getExtraPropertyID();
		RoomListFragment fragment = RoomListFragment.newInstance(propertyID);
		if (propertyID == Property.ID_ADD) {
			setActionBarSubtitle(null);
			setActionBarTitle(getText(R.string.room_list));
		} else {
			fragment.setHeader(PropertyViewFragment.newInstance(propertyID));
		}
		return fragment;
	}

	public void propertyLoaded(PropertyDTO property) {
		setActionBarTitle(property.name);
	}

	public void propertyDeleted(PropertyDTO property) {
		finish();
	}

	public void newRoom(long propertyID) {
		startActivity(RoomEditActivity.add(propertyID));
	}

	public void roomSelected(long roomID) {
		startActivity(RoomViewActivity.show(roomID));
	}

	public void roomActioned(long roomID) {
		startActivity(RoomEditActivity.edit(roomID));
	}

	private long getExtraPropertyID() {
		return getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static Intent listAll() {
		return show(Property.ID_ADD);
	}
	public static Intent show(long propertyID) {
		Intent intent = new Intent(App.getAppContext(), PropertyViewActivity.class);
		intent.putExtra(Extras.PROPERTY_ID, propertyID);
		return intent;
	}
}
