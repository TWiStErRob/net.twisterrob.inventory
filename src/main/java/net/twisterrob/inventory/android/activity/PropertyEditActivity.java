package net.twisterrob.inventory.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

	private static class Params {
		long propertyID;

		boolean editExisting() {
			return propertyID != Property.ID_ADD;
		}

		void fill(Intent intent) {
			propertyID = intent.getLongExtra(EXTRA_PROPERTY_ID, Property.ID_ADD);
		}
	}
	private final Params params = new Params();

	private static class ViewHolder {
		Spinner propertyType;
		EditText propertyName;

		void fill(Activity root) {
			propertyName = (EditText)root.findViewById(R.id.propertyName);
			propertyType = (Spinner)root.findViewById(R.id.propertyType);

		}
	}
	private final ViewHolder view = new ViewHolder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.property_edit);

		view.fill(this);
		params.fill(getIntent());

		CursorAdapter adapter = Adapters.loadCursorAdapter(this, R.xml.property_types, (Cursor)null);
		getSupportLoaderManager().initLoader(Loaders.PropertyTypes.ordinal(), null, new CursorSwapper(this, adapter));

		view.propertyType.setAdapter(adapter);
		view.propertyType.setOnItemSelectedListener(new DefaultValueUpdater(view.propertyName, Property.NAME));

		updateEditedItem();
	}

	@Override
	protected Cursor getEditedItem() {
		Cursor property = null;
		if (params.editExisting()) {
			property = App.getInstance().getDataBase().getProperty(params.propertyID);
		}
		return property;
	}

	@Override
	protected void fillEditedItem(Cursor item) {
		String name = item.getString(item.getColumnIndex(Property.NAME));
		long type = item.getLong(item.getColumnIndex(Property.TYPE));

		setTitle(name);
		view.propertyName.setText(name);
		AndroidTools.selectByID(view.propertyType, type);
	}
}
