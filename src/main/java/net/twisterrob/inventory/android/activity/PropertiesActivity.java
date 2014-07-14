package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class PropertiesActivity extends FragmentActivity {
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_list);

		CursorAdapter propertiesAdapter = new SimpleCursorAdapter(this, R.layout.property_item, null, new String[]{
				Property.NAME, "image"}, new int[]{R.id.propertyName, R.id.propertyImage}, 0);
		getSupportLoaderManager().initLoader(Loaders.Properties.ordinal(), null,
				new CursorSwapper(this, propertiesAdapter));

		GridView properties = (GridView)findViewById(R.id.properties);
		properties.setAdapter(propertiesAdapter);

		properties.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				Intent intent = new Intent(getApplicationContext(), PropertyEditActivity.class);
				intent.putExtra(PropertyEditActivity.EXTRA_PROPERTY_ID, id);
				startActivity(intent);
				return true;
			}
		});
		properties.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				if (id == Property.ID_ADD) {
					Intent intent = new Intent(getApplicationContext(), PropertyEditActivity.class);
					startActivity(intent);
				} else {
					Intent intent = new Intent(getApplicationContext(), RoomsActivity.class);
					intent.putExtra(RoomsActivity.EXTRA_PROPERTY_ID, id);
					startActivity(intent);
				}
			}
		});
	}
}
