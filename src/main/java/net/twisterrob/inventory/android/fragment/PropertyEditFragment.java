package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.view.*;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyEditFragment extends BaseEditFragment {
	private long currentPropertyID;
	private Spinner propertyType;
	private EditText propertyName;
	private CursorAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.property_edit, container, false);
		propertyName = (EditText)root.findViewById(R.id.propertyName);
		propertyType = (Spinner)root.findViewById(R.id.propertyType);
		adapter = Adapters.loadCursorAdapter(getActivity(), R.xml.property_types, (Cursor)null);
		((Button)root.findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		propertyType.setAdapter(adapter);
		propertyType.setOnItemSelectedListener(new DefaultValueUpdater(propertyName, Property.NAME));

		return root;
	}

	@Override
	public void load(long id) {
		currentPropertyID = id;
		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper typeCursorSwapper = new CursorSwapper(getActivity(), adapter);
		Dependency<Cursor> populateTypes = manager.add(PropertyTypes.ordinal(), null, typeCursorSwapper);

		if (id != Property.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.PROPERTY_ID, id);
			Dependency<Cursor> loadPropertyData = manager.add(SingleProperty.ordinal(), args, new PropertyLoaded());

			loadPropertyData.dependsOn(populateTypes); // type is auto-selected when a property is loaded
		}
		manager.startLoading();
	}

	@Override
	public void save() {
		new SaveTask().execute(getCurrentProperty());
	}

	private PropertyDTO getCurrentProperty() {
		PropertyDTO property = new PropertyDTO();
		property.id = currentPropertyID;
		property.name = propertyName.getText().toString();
		property.type = propertyType.getSelectedItemId();
		return property;
	}

	private class PropertyLoaded extends LoadSingleRow {
		PropertyLoaded() {
			super(getActivity());
		}

		@Override
		protected void process(Cursor item) {
			super.process(item);
			PropertyDTO property = PropertyDTO.fromCursor(item);

			getActivity().setTitle(property.name);
			AndroidTools.selectByID(propertyType, property.type);
			propertyName.setText(property.name); // must set it after propertyType to prevent auto-propagation
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
	}

	private final class SaveTask extends SimpleAsyncTask<PropertyDTO, Void, Long> {
		@Override
		protected Long doInBackground(PropertyDTO param) {
			try {
				Database db = App.getInstance().getDataBase();
				if (param.id == Property.ID_ADD) {
					return db.newProperty(param.name, param.type);
				} else {
					db.updateProperty(param.id, param.name, param.type);
					return param.id;
				}
			} catch (SQLiteConstraintException ex) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Long result) {
			if (result != null) {
				getActivity().finish();
			} else {
				Toast.makeText(getActivity(), "Property name must be unique", Toast.LENGTH_LONG).show();
			}
		}
	}
}
