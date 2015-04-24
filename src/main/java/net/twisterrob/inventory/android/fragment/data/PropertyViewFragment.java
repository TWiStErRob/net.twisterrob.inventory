package net.twisterrob.inventory.android.fragment.data;

import java.util.Date;

import org.slf4j.*;

import android.database.Cursor;
import android.view.MenuItem;

import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.data.PropertyViewFragment.PropertyEvents;
import net.twisterrob.inventory.android.tasks.DeletePropertiesAction;
import net.twisterrob.inventory.android.view.Dialogs;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyViewFragment extends BaseViewFragment<PropertyDTO, PropertyEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyViewFragment.class);

	public interface PropertyEvents {
		void propertyLoaded(PropertyDTO property);
		void propertyDeleted(PropertyDTO property);
	}

	public PropertyViewFragment() {
		setDynamicResource(DYN_EventsClass, PropertyEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.property);
		setDynamicResource(DYN_TypeLoader, Loaders.PropertyTypes);
		setDynamicResource(DYN_TypeChangeTitle, "Change Type");
	}

	@Override
	protected void onRefresh() {
		super.onRefresh();
		getLoaderManager().getLoader(SingleProperty.id()).onContentChanged();
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		getLoaderManager().initLoader(SingleProperty.id(),
				ExtrasFactory.bundleFromProperty(getArgPropertyID()), new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		PropertyDTO property = PropertyDTO.fromCursor(cursor);
		super.onSingleRowLoaded(property);
		eventsListener.propertyLoaded(property);
	}

	@Override
	protected CharSequence getDetailsString(PropertyDTO entity, boolean DEBUG) {
		return new DescriptionBuilder()
				.append("Property ID", entity.id, DEBUG)
				.append("Property Name", entity.name)
				.append("Property Type", entity.type, DEBUG)
				.append("# of rooms", entity.numDirectChildren)
				.append("# of rooms", entity.numAllChildren, DEBUG)
				.append("# of items in the rooms", entity.numDirectItems)
				.append("# of items inside rooms", entity.numAllItems)
				.append(entity.hasImage? "image" : "image removed", new Date(entity.imageTime), DEBUG)
				.append("Description", entity.description)
				.build();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_edit:
				startActivity(PropertyEditActivity.edit(getArgPropertyID()));
				return true;
			case R.id.action_property_delete:
				delete(getArgPropertyID());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void delete(final long propertyID) {
		Dialogs.executeConfirm(getActivity(), new DeletePropertiesAction(propertyID) {
			@Override public void finished() {
				PropertyDTO item = new PropertyDTO();
				item.id = propertyID;
				eventsListener.propertyDeleted(item);
			}
		});
	}

	@Override protected void editImage() {
		startActivity(BaseEditActivity.takeImage(PropertyEditActivity.edit(getArgPropertyID())));
	}

	@Override protected void update(PropertyDTO entity, long newType) {
		App.db().updateProperty(entity.id, newType, entity.name, entity.description);
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static PropertyViewFragment newInstance(long propertyID) {
		if (propertyID == Property.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing property");
		}

		PropertyViewFragment fragment = new PropertyViewFragment();
		fragment.setArguments(ExtrasFactory.bundleFromProperty(propertyID));
		return fragment;
	}
}
