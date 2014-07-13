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
				Properties.NAME, "image"}, new int[]{R.id.propertyName, R.id.propertyImage}, 0);
		getSupportLoaderManager().initLoader(Loaders.Properties.ordinal(), null,
				new CursorSwapper(this, propertiesAdapter));

		GridView properties = (GridView)findViewById(R.id.properties);
		properties.setAdapter(propertiesAdapter);

		properties.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Cursor cursor = (Cursor)parent.getAdapter().getItem(position);
				LOG.debug("Clicked on #{}", id);
				Intent intent = new Intent(getApplicationContext(), PropertyEditActivity.class);
				intent.putExtra(PropertyEditActivity.EXTRA_ID, id);
				startActivity(intent);
			}
		});
	}
}
