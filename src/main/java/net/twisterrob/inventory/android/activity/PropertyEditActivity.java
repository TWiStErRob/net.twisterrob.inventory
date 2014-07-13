package net.twisterrob.inventory.android.activity;

import android.database.*;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class PropertyEditActivity extends FragmentActivity {
	public static final String EXTRA_ID = "propertyID";

	private long propertyID;
	private int preselectedPropertyType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_edit);
		final EditText propertyName = (EditText)findViewById(R.id.propertyName);
		final Spinner propertyType = (Spinner)findViewById(R.id.propertyType);

		propertyID = getIntent().getLongExtra(EXTRA_ID, Properties.ID_ADD);

		CursorAdapter propertiesTypeAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
				new String[]{PropertyType.NAME}, new int[]{android.R.id.text1}, 0);
		getSupportLoaderManager().initLoader(Loaders.PropertyTypes.ordinal(), null,
				new CursorSwapper(this, propertiesTypeAdapter) {
					@Override
					public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
						super.onLoadFinished(loader, data);
						propertyType.setSelection(preselectedPropertyType);
					}
				});

		propertyType.setAdapter(propertiesTypeAdapter);

		if (propertyID != Properties.ID_ADD) {
			Cursor property = App.getInstance().getDataBase().getProperty(propertyID);
			DatabaseUtils.dumpCursor(property);
			if (property.getCount() == 1) {
				property.moveToFirst();
				propertyName.setText(property.getString(property.getColumnIndex(Properties.NAME)));
				preselectedPropertyType = (int)property.getLong(property.getColumnIndex(Properties.TYPE));
			} else {
				String msg = "Property #" + propertyID + " not found!";
				Toast.makeText(PropertyEditActivity.this, msg, Toast.LENGTH_LONG).show();
			}
			property.close();
		}
	}
}
