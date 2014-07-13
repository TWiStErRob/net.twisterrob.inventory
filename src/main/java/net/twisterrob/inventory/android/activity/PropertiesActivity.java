package net.twisterrob.inventory.android.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.*;
import android.widget.GridView;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class PropertiesActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_list);

		CursorAdapter propertiesAdapter = new SimpleCursorAdapter(this, R.layout.property_item, null, new String[]{
				Properties.NAME, "image"}, new int[]{R.id.propertyName, R.id.propertyImage}, 0);
		getSupportLoaderManager().initLoader(Loaders.Properties.ordinal(), null,
				new CursorSwapper(this, propertiesAdapter));

		GridView properties = (GridView)findViewById(R.id.properties);
		properties.setAdapter(propertiesAdapter);
	}
}
