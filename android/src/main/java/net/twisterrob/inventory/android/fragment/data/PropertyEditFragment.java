package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.support.annotation.NonNull;

import net.twisterrob.android.content.loader.DynamicLoaderManager;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.data.PropertyEditFragment.PropertyEditEvents;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyEditFragment extends BaseEditFragment<PropertyEditEvents, PropertyDTO> {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyEditFragment.class);

	public interface PropertyEditEvents {
		void propertyLoaded(PropertyDTO property);
		void propertySaved(long propertyID);
	}

	public PropertyEditFragment() {
		setDynamicResource(DYN_EventsClass, PropertyEditEvents.class);
		setDynamicResource(DYN_NameHintResource, R.string.property_name_hint);
		setDynamicResource(DYN_DescriptionHintResource, R.string.property_description_hint);
		setKeepNameInSync(true);
	}

	protected @NonNull TypeAdapter createTypeAdapter() {
		TypeAdapter adapter = super.createTypeAdapter();
		adapter.setDisplayKeywords(true);
		return adapter;
	}

	@Override protected void onStartLoading() {
		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		Dependency<Cursor> populateTypes = manager.add(PropertyTypes.id(), null, getTypeCallback());

		if (!isNew()) {
			Dependency<Cursor> loadPropertyData = manager.add(SingleProperty.id(),
					Intents.bundleFromProperty(getArgPropertyID()),
					SingleProperty.createCallbacks(getContext(), new SingleRowLoaded())
			);
			loadPropertyData.dependsOn(populateTypes); // type is auto-selected when a property is loaded
		}

		manager.startLoading();
	}

	@Override protected void onSingleRowLoaded(@NonNull Cursor cursor) {
		PropertyDTO property = PropertyDTO.fromCursor(cursor);
		onSingleRowLoaded(property);
		eventsListener.propertyLoaded(property);
	}

	@Override protected PropertyDTO createDTO() {
		PropertyDTO property = new PropertyDTO();
		property.id = getArgPropertyID();
		return property;
	}

	@Override protected boolean isNew() {
		return getArgPropertyID() == Property.ID_ADD;
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	@Override protected PropertyDTO onSave(Database db, PropertyDTO param) {
		if (param.id == Property.ID_ADD) {
			param.id = db.createProperty(param.type, param.name, param.description);
		} else {
			db.updateProperty(param.id, param.type, param.name, param.description);
		}
		if (!param.hasImage) {
			// may clear already cleared images, but there's not enough info
			db.setPropertyImage(param.id, null);
		} else if (param.image != null) {
			db.setPropertyImage(param.id, db.addImage(param.image, null));
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
