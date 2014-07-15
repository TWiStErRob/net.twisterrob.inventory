package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.widget.*;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.db.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class PropertyEditActivity extends BaseEditActivity {
	public static final String EXTRA_PROPERTY_ID = "propertyID";

	private static class Params {
		long propertyID;

		void fill(Intent intent) {
			propertyID = intent.getLongExtra(EXTRA_PROPERTY_ID, Property.ID_ADD);
		}

		Bundle toBundle() {
			Bundle bundle = new Bundle();
			bundle.putLong(EXTRA_PROPERTY_ID, propertyID);
			return bundle;
		}
	}
	private final Params params = new Params();

	Spinner propertyType;
	EditText propertyName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		params.fill(getIntent());

		super.setContentView(R.layout.property_edit);
		propertyName = (EditText)findViewById(R.id.propertyName);
		propertyType = (Spinner)findViewById(R.id.propertyType);

		CursorAdapter adapter = Adapters.loadCursorAdapter(this, R.xml.property_types, (Cursor)null);
		propertyType.setAdapter(adapter);
		propertyType.setOnItemSelectedListener(new DefaultValueUpdater(propertyName, Property.NAME));

		InitAction<Cursor> propertyTypes = new InitAction<Cursor>(getSupportLoaderManager(),
				Loaders.PropertyTypes.ordinal(), null, new CursorSwapper(this, adapter));

		InitAction<Cursor> singleProperty = new InitAction<Cursor>(getSupportLoaderManager(),
				Loaders.SingleProperty.ordinal(), params.toBundle(), new LoadProperty()) {
			@Override
			public void run() {
				if (getArgs().getLong(EXTRA_PROPERTY_ID, Property.ID_ADD) != Property.ID_ADD) {
					super.run();
				}
			}
		};

		LoaderChain.sequence(propertyTypes, singleProperty).run();
	}
	private class LoadProperty extends LoadSingleRow {
		LoadProperty() {
			super(PropertyEditActivity.this);
		}

		@Override
		protected void process(Cursor item) {
			super.process(item);
			String name = item.getString(item.getColumnIndex(Property.NAME));
			long type = item.getLong(item.getColumnIndex(Property.TYPE));

			setTitle(name);
			propertyName.setText(name);
			AndroidTools.selectByID(propertyType, type);
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			finish();
		}
	}
}
