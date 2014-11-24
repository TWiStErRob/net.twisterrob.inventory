package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.BaseDetailActivity.NoFragment;
import net.twisterrob.inventory.android.fragment.data.PropertyListFragment;
import net.twisterrob.inventory.android.fragment.data.PropertyListFragment.PropertiesEvents;

public class PropertyListActivity extends BaseDetailActivity<NoFragment, PropertyListFragment>
		implements PropertiesEvents {
	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		setActionBarSubtitle(null);
		setActionBarTitle(getTitle());
		setIcon(R.raw.property_unknown);
		hideDetails();
		setFragments(null, PropertyListFragment.newInstance());
	}

	public void newProperty() {
		startActivity(PropertyEditActivity.add());
	}

	public void propertySelected(long id) {
		startActivity(PropertyViewActivity.show(id));
	}

	public void propertyActioned(long id) {
		startActivity(PropertyEditActivity.edit(id));
	}

	public static Intent listAll() {
		Intent intent = new Intent(App.getAppContext(), PropertyListActivity.class);
		return intent;
	}
}
