package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.PropertiesFragment.PropertyEvents;

public class PropertiesActivity extends BaseListActivity implements PropertyEvents {
	private PropertiesFragment properties;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.properties);

		properties = PropertiesFragment.newInstance();

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.properties, properties);
		ft.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		properties.refresh();
	}

	public void newProperty() {
		startActivity(PropertyEditActivity.add());
	}

	public void propertySelected(long id) {
		startActivity(RoomsActivity.list(id));
	}

	public void propertyActioned(long id) {
		startActivity(PropertyEditActivity.edit(id));
	}

	public static Intent list() {
		Intent intent = new Intent(App.getAppContext(), PropertiesActivity.class);
		return intent;
	}
}
