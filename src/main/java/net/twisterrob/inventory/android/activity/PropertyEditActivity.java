package net.twisterrob.inventory.android.activity;

import android.database.*;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.widget.*;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class PropertyEditActivity extends BaseEditActivity {
	public static final String EXTRA_PROPERTY_ID = "propertyID";

	private long propertyID;
	private int preselectedPropertyType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_edit);
		final EditText propertyName = (EditText)findViewById(R.id.propertyName);
		final Spinner propertyType = (Spinner)findViewById(R.id.propertyType);

		propertyID = getIntent().getLongExtra(EXTRA_PROPERTY_ID, Property.ID_ADD);

		CursorAdapter adapter = Adapters.loadCursorAdapter(this, R.xml.property_types, (Cursor)null);
		getSupportLoaderManager().initLoader(Loaders.PropertyTypes.ordinal(), null,
				new CursorSwapper(this, adapter) {
					@Override
					public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
						super.onLoadFinished(loader, data);
						AndroidTools.selectByID(propertyType, preselectedPropertyType);
					}
				});

		propertyType.setAdapter(adapter);

		if (propertyID != Property.ID_ADD) {
			Cursor property = App.getInstance().getDataBase().getProperty(propertyID);
			DatabaseUtils.dumpCursor(property);
			if (property.getCount() == 1) {
				property.moveToFirst();
				String name = property.getString(property.getColumnIndex(Property.NAME));
				propertyName.setText(name);
				setTitle(name);
				preselectedPropertyType = (int)property.getLong(property.getColumnIndex(Property.TYPE));
			} else {
				String msg = "Property #" + propertyID + " not found!";
				Toast.makeText(PropertyEditActivity.this, msg, Toast.LENGTH_LONG).show();
				finish();
			}
			property.close();
		}
	}
}
