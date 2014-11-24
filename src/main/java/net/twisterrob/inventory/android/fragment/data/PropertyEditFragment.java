package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.*;

import net.twisterrob.android.content.loader.DynamicLoaderManager;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.view.CursorSwapper;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyEditFragment extends BaseEditFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyEditFragment.class);

	@Override
	protected String getBaseFileName() {
		return "Property_" + getArgPropertyID();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setKeepNameInSync(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_property_edit, container, false);
	}

	@Override
	protected void onStartLoading() {
		long id = getArgPropertyID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper typeCursorSwapper = new CursorSwapper(getContext(), typeAdapter);
		Dependency<Cursor> populateTypes = manager.add(PropertyTypes.ordinal(), null, typeCursorSwapper);

		if (id != Property.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.PROPERTY_ID, id);
			Dependency<Cursor> loadPropertyData = manager.add(SingleProperty.ordinal(), args, new SingleRowLoaded());

			loadPropertyData.dependsOn(populateTypes); // type is auto-selected when a property is loaded
		} else {
			getBaseActivity().setActionBarTitle(getString(R.string.property_new));
		}

		manager.startLoading();
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		PropertyDTO property = PropertyDTO.fromCursor(cursor);
		onSingleRowLoaded(property, property.type);
	}

	@Override
	protected void save() {
		new SaveTask().execute(getCurrentProperty());
	}

	private PropertyDTO getCurrentProperty() {
		PropertyDTO property = new PropertyDTO();
		property.id = getArgPropertyID();
		property.name = title.getText().toString();
		property.type = type.getSelectedItemId();
		property.setImage(getContext(), getCurrentImage());
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
					return db.createProperty(param.name, param.type, param.image);
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
				App.toast("Property name must be unique");
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
