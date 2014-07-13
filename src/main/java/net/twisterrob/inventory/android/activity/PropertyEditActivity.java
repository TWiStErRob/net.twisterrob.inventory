package net.twisterrob.inventory.android.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class PropertyEditActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_edit);

		CursorAdapter propertiesTypeAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
				new String[]{PropertyType.NAME}, new int[]{android.R.id.text1}, 0);
		getSupportLoaderManager().initLoader(Loaders.PropertyTypes.ordinal(), null,
				new CursorSwapper(this, propertiesTypeAdapter));

		Spinner propertyType = (Spinner)findViewById(R.id.propertyType);
		propertyType.setAdapter(propertiesTypeAdapter);
		propertyType.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				@SuppressWarnings("resource")
				Cursor cursor = ((CursorAdapter)parent.getAdapter()).getCursor();
				cursor.moveToPosition(position);
				String text = cursor.getString(cursor.getColumnIndex(PropertyType.ID));
				Toast.makeText(PropertyEditActivity.this, text, Toast.LENGTH_LONG).show();
			}
			public void onNothingSelected(AdapterView<?> parent) {
				// ignore
			}
		});
	}
}
