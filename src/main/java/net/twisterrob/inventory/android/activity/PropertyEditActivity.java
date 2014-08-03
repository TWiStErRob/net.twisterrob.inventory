package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.PropertyEditFragment;

public class PropertyEditActivity extends BaseEditActivity<PropertyEditFragment> {
	@Override
	protected PropertyEditFragment onCreateFragment(Bundle savedInstanceState) {
		return PropertyEditFragment.newInstance(getExtraPropertyID());
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
