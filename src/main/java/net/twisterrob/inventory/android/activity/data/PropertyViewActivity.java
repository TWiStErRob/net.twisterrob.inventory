package net.twisterrob.inventory.android.activity.data;

import org.slf4j.*;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.data.PropertyViewFragment.PropertyEvents;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;

public class PropertyViewActivity extends BaseDetailActivity<RoomListFragment> implements PropertyEvents, RoomsEvents {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyViewActivity.class);

	private PropertyDTO current;

	@Override
	protected RoomListFragment onCreateFragment(Bundle savedInstanceState) {
		return RoomListFragment.newInstance(getExtraPropertyID()).addHeader();
	}

	public void propertyLoaded(PropertyDTO property) {
		setActionBarTitle(property.name);
		current = property;
	}
	public void propertyDeleted(PropertyDTO property) {
		finish();
	}

	public void newRoom(long propertyID) {
		startActivity(RoomEditActivity.add(propertyID));
	}
	public void roomSelected(long roomID) {
		startActivity(Intents.childNav(RoomViewActivity.show(roomID)));
	}
	public void roomActioned(long roomID) {
		startActivity(RoomEditActivity.edit(roomID));
	}

	@Override
	protected String checkExtras() {
		if (getExtraPropertyID() == Property.ID_ADD) {
			return "Invalid property ID";
		}
		return null;
	}

	@Override public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	@Override public void onContentChanged() {
		super.onContentChanged();
		setupTitleEditor();
	}

	@Override protected void updateName(String newName) {
		App.db().updateProperty(current.id, current.type, newName, current.description);
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
