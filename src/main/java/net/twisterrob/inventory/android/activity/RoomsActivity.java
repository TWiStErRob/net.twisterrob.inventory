package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.RoomsFragment.RoomEvents;

public class RoomsActivity extends BaseListActivity implements RoomEvents {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.rooms);

		RoomsFragment rooms = getFragment(R.id.rooms);
		rooms.listForProperty(getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD));
	}

	public void newRoom() {
		startActivity(RoomEditActivity.add());
	}

	public void roomSelected(long id, long rootItemID) {
		ItemsFragment list = getFragment(R.id.items);
		if (list != null && list.isInLayout()) {
			list.list(rootItemID);
		} else {
			startActivity(ItemsActivity.list(rootItemID));
		}
	}

	public void roomActioned(long id) {
		RoomEditFragment editor = getFragment(R.id.room);
		if (editor != null && editor.isInLayout()) {
			editor.load(id);
		} else {
			startActivity(RoomEditActivity.edit(id));
		}
	}

	public static Intent list(long propertyId) {
		Intent intent = new Intent(App.getAppContext(), RoomsActivity.class);
		intent.putExtra(Extras.PROPERTY_ID, propertyId);
		return intent;
	}
}
