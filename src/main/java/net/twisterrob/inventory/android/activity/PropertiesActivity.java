package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.PropertiesFragment.PropertyEvents;

public class PropertiesActivity extends BaseListActivity implements PropertyEvents {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.properties);

		PropertiesFragment properties = getFragment(R.id.properties);
		properties.list();
	}

	public void newProperty() {
		startActivity(PropertyEditActivity.add());
	}

	public void propertySelected(long id) {
		RoomsFragment rooms = getFragment(R.id.rooms);
		if (rooms != null && rooms.isInLayout()) {
			rooms.listForProperty(id);
		} else {
			startActivity(RoomsActivity.list(id));
		}
	}

	public void propertyActioned(long id) {
		PropertyEditFragment editor = getFragment(R.id.property);
		if (editor != null && editor.isInLayout()) {
			editor.load(id);
		} else {
			startActivity(PropertyEditActivity.edit(id));
		}
	}

	public static Intent list() {
		Intent intent = new Intent(App.getAppContext(), PropertiesActivity.class);
		return intent;
	}
}
