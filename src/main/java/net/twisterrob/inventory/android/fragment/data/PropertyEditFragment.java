package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import net.twisterrob.android.content.loader.DynamicLoaderManager;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.data.PropertyEditFragment.PropertyEditEvents;
import net.twisterrob.inventory.android.view.CursorSwapper;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyEditFragment extends BaseEditFragment<PropertyEditEvents, PropertyDTO> {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyEditFragment.class);

	public interface PropertyEditEvents {
		void propertyLoaded(PropertyDTO property);
		void propertySaved(long propertyID);
	}

	public PropertyEditFragment() {
		setDynamicResource(DYN_EventsClass, PropertyEditEvents.class);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setKeepNameInSync(true);
	}

	@Override public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);
		name.setHint(R.string.property_name_hint);
		description.setHint(R.string.property_description_hint);
	}

	@Override protected void onStartLoading() {
		long id = getArgPropertyID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		CursorSwapper typeCursorSwapper = new CursorSwapper(getContext(), typeAdapter);
		Dependency<Cursor> populateTypes = manager.add(PropertyTypes.id(), null, typeCursorSwapper);

		if (id != Property.ID_ADD) {
			Dependency<Cursor> loadPropertyData = manager.add(SingleProperty.id(),
					Intents.bundleFromProperty(id), new SingleRowLoaded());
			loadPropertyData.dependsOn(populateTypes); // type is auto-selected when a property is loaded
		}

		manager.startLoading();
	}

	@Override protected void onSingleRowLoaded(Cursor cursor) {
		PropertyDTO property = PropertyDTO.fromCursor(cursor);
		onSingleRowLoaded(property);
		eventsListener.propertyLoaded(property);
	}

	@Override protected PropertyDTO createDTO() {
		PropertyDTO property = new PropertyDTO();
		property.id = getArgPropertyID();
		return property;
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	@Override protected PropertyDTO onSave(Database db, PropertyDTO param) throws Exception {
		if (param.id == Property.ID_ADD) {
			param.id = db.createProperty(param.type, param.name, param.description);
		} else {
			db.updateProperty(param.id, param.type, param.name, param.description);
		}
		if (!param.hasImage) {
			// may clear already cleared images, but there's not enough info
			db.setPropertyImage(param.id, null, null);
		} else if (param.image != null) {
			db.setPropertyImage(param.id, param.image, null);
		} else {
			// it has an image, but there's no blob -> the image is already in DB
		}
		return param;
	}

	@Override protected void onSaved(PropertyDTO result) {
		eventsListener.propertySaved(result.id);
	}

	public static PropertyEditFragment newInstance(long propertyID) {
		PropertyEditFragment fragment = new PropertyEditFragment();
		fragment.setArguments(Intents.bundleFromProperty(propertyID));
		return fragment;
	}
}
