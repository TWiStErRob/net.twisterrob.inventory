package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.data.PropertyEditFragment;

public class PropertyEditActivity extends BaseEditActivity<PropertyEditFragment>
		implements PropertyEditFragment.PropertyEditEvents {
	@Override
	protected PropertyEditFragment onCreateFragment(Bundle savedInstanceState) {
		setActionBarTitle(getString(R.string.property_new));
		return PropertyEditFragment.newInstance(getExtraPropertyID());
	}

	@Override public void propertyLoaded(PropertyDTO property) {
		setActionBarTitle(property.name);
	}

	@Override public void propertySaved(long propertyID) {
		Intent data = Intents.intentFromProperty(propertyID);
		setResult(RESULT_OK, data);
		finish();
	}

	private long getExtraPropertyID() {
		return getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static Intent add() {
		Intent intent = new Intent(App.getAppContext(), PropertyEditActivity.class);
		return intent;
	}

	public static Intent edit(long propertyId) {
		Intent intent = new Intent(App.getAppContext(), PropertyEditActivity.class);
		intent.putExtra(Extras.PROPERTY_ID, propertyId);
		return intent;
	}
}
