package net.twisterrob.android.inventory;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

public class BuildingEditActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.building_edit);

		CursorAdapter buildingTypeAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
				new String[]{BuildingType.NAME}, new int[]{android.R.id.text1}, 0);
		getSupportLoaderManager().initLoader(Loaders.BuildingTypes.ordinal(), null,
				new CursorSwapper(this, buildingTypeAdapter));

		Spinner buildingType = (Spinner)findViewById(R.id.buildingType);
		buildingType.setAdapter(buildingTypeAdapter);
		buildingType.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				@SuppressWarnings("resource")
				Cursor cursor = ((CursorAdapter)parent.getAdapter()).getCursor();
				cursor.moveToPosition(position);
				String text = cursor.getString(cursor.getColumnIndex(BuildingType.ID));
				Toast.makeText(BuildingEditActivity.this, text, Toast.LENGTH_LONG).show();
			}
			public void onNothingSelected(AdapterView<?> parent) {
				// ignore
			}
		});
	}
}
