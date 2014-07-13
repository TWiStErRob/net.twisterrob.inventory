package net.twisterrob.android.inventory;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.*;
import android.widget.GridView;

public class BuildingsActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.building_list);

		CursorAdapter buildingsAdapter = new SimpleCursorAdapter(this, R.layout.building_item, null, new String[]{
				Building.NAME, "image"}, new int[]{R.id.buildingName, R.id.buildingImage}, 0);
		getSupportLoaderManager().initLoader(Loaders.Buildings.ordinal(), null,
				new CursorSwapper(this, buildingsAdapter));

		GridView buildings = (GridView)findViewById(R.id.buildings);
		buildings.setAdapter(buildingsAdapter);
	}
}
