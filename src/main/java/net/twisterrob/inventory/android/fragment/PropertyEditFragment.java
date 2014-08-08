package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.view.*;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyEditFragment extends BaseEditFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyEditFragment.class);

	private EditText propertyName;
	private Spinner propertyType;
	private CursorAdapter adapter;

	public PropertyEditFragment() {
		setDynamicResource(DYN_ImageView, R.id.propertyImage);
	}

	@Override
	protected String getBaseFileName() {
		return "Property_" + getArgPropertyID();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.property_edit, container, false);
		propertyName = (EditText)root.findViewById(R.id.propertyName);
		propertyType = (Spinner)root.findViewById(R.id.propertyType);

		((Button)root.findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		adapter = new TypeAdapter(getActivity());
		propertyType.setAdapter(adapter);
		propertyType.setOnItemSelectedListener(new DefaultValueUpdater(propertyName, Property.NAME));

		return root;
	}

	@Override
	protected void onStartLoading() {
		long id = getArgPropertyID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper typeCursorSwapper = new CursorSwapper(getActivity(), adapter);
		Dependency<Cursor> populateTypes = manager.add(PropertyTypes.ordinal(), null, typeCursorSwapper);

		if (id != Property.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.PROPERTY_ID, id);
			Dependency<Cursor> loadPropertyData = manager.add(SingleProperty.ordinal(), args, new SingleRowLoaded());

			loadPropertyData.dependsOn(populateTypes); // type is auto-selected when a property is loaded
		} else {
			setCurrentImageDriveId(null, R.drawable.image_add);
		}
		manager.startLoading();
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		PropertyDTO property = PropertyDTO.fromCursor(cursor);

		getActivity().setTitle(property.name);
		AndroidTools.selectByID(propertyType, property.type);
		propertyName.setText(property.name); // must set it after propertyType to prevent auto-propagation

		setCurrentImageDriveId(property.image, property.getFallbackDrawable(getActivity()));
	}

	private void save() {
		new SaveTask().execute(getCurrentProperty());
	}

	private PropertyDTO getCurrentProperty() {
		PropertyDTO property = new PropertyDTO();
		property.id = getArgPropertyID();
		property.name = propertyName.getText().toString();
		property.type = propertyType.getSelectedItemId();
		property.image = getCurrentImageDriveId();
		return property;
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	private final class SaveTask extends SimpleAsyncTask<PropertyDTO, Void, Long> {
		@Override
		protected Long doInBackground(PropertyDTO param) {
			try {
				Database db = App.db();
				if (param.id == Property.ID_ADD) {
					return db.newProperty(param.name, param.type, param.image);
				} else {
					db.updateProperty(param.id, param.name, param.type, param.image);
					return param.id;
				}
			} catch (SQLiteConstraintException ex) {
				LOG.warn("Cannot save {}", param, ex);
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

	public static PropertyEditFragment newInstance(long propertyID) {
		PropertyEditFragment fragment = new PropertyEditFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, propertyID);

		fragment.setArguments(args);
		return fragment;
	}
}
