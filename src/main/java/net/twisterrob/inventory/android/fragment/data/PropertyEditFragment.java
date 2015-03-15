package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.View;

import net.twisterrob.android.content.loader.DynamicLoaderManager;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.data.PropertyEditFragment.PropertyEditEvents;
import net.twisterrob.inventory.android.view.CursorSwapper;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyEditFragment extends BaseEditFragment<PropertyEditEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyEditFragment.class);

	public interface PropertyEditEvents {
		void propertyLoaded(PropertyDTO property);
		void propertySaved(long propertyID);
	}

	public PropertyEditFragment() {
		setDynamicResource(DYN_EventsClass, PropertyEditEvents.class);
	}

	@Override
	protected String getBaseFileName() {
		return "Property_" + getArgPropertyID();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setKeepNameInSync(true);
	}

	@Override public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);
		name.setHint(R.string.property_name_hint);
		description.setHint(R.string.property_description_hint);
	}

	@Override
	protected void onStartLoading() {
		long id = getArgPropertyID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper typeCursorSwapper = new CursorSwapper(getContext(), typeAdapter);
		Dependency<Cursor> populateTypes = manager.add(PropertyTypes.id(), null, typeCursorSwapper);

		if (id != Property.ID_ADD) {
			Dependency<Cursor> loadPropertyData = manager.add(SingleProperty.id(),
					ExtrasFactory.bundleFromProperty(id), new SingleRowLoaded());
			loadPropertyData.dependsOn(populateTypes); // type is auto-selected when a property is loaded
		}

		manager.startLoading();
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		PropertyDTO property = PropertyDTO.fromCursor(cursor);
		onSingleRowLoaded(property);
		eventsListener.propertyLoaded(property);
	}

	@Override
	protected void doSave() {
		new SaveTask().execute(getCurrentProperty());
	}

	private PropertyDTO getCurrentProperty() {
		PropertyDTO property = new PropertyDTO();
		property.id = getArgPropertyID();
		property.name = name.getText().toString();
		property.description = description.getText().toString();
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
					return db.createProperty(param.type, param.name, param.description, param.image);
				} else {
					db.updateProperty(param.id, param.type, param.name, param.description, param.image);
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
				eventsListener.propertySaved(result);
			} else {
				App.toast("Property name must be unique");
			}
		}
	}

	public static PropertyEditFragment newInstance(long propertyID) {
		PropertyEditFragment fragment = new PropertyEditFragment();
		fragment.setArguments(ExtrasFactory.bundleFromProperty(propertyID));
		return fragment;
	}
}
