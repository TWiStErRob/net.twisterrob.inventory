package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
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
		return inflater.inflate(R.layout.property_edit, container, false);
	}

	@Override
	protected void onStartLoading() {
		long id = getArgPropertyID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper typeCursorSwapper = new CursorSwapper(getActivity(), typeAdapter);
		Dependency<Cursor> populateTypes = manager.add(PropertyTypes.ordinal(), null, typeCursorSwapper);

		if (id != Property.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.PROPERTY_ID, id);
			Dependency<Cursor> loadPropertyData = manager.add(SingleProperty.ordinal(), args, new SingleRowLoaded());

			loadPropertyData.dependsOn(populateTypes); // type is auto-selected when a property is loaded
		} else {
			setTitle(R.string.property_new);
			setCurrentImageDriveId(null, R.drawable.image_add);
		}
		manager.startLoading();
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		PropertyDTO property = PropertyDTO.fromCursor(cursor);

		setTitle(property.name);
		AndroidTools.selectByID(type, property.type);
		title.setText(property.name); // must set it after propertyType to prevent auto-propagation

		setCurrentImageDriveId(property.image, property.getFallbackDrawable(getActivity()));
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
