package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.PropertyEditFragment;

public class PropertyEditActivity extends BaseEditActivity {
	private PropertyEditFragment editor;
	private long currentPropertyID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property);

		editor = getFragment(R.id.property);
		currentPropertyID = getIntent().getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	@Override
	protected void onStart() {
		super.onStart();
		editor.load(currentPropertyID);
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
