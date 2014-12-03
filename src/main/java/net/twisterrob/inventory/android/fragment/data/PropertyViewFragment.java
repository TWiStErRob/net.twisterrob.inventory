package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.view.MenuItem;

import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.PropertyEditActivity;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.data.PropertyViewFragment.PropertyEvents;
import net.twisterrob.inventory.android.tasks.DeletePropertyTask;
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
	}

	@Override
	protected void onRefresh() {
		super.onRefresh();
		getLoaderManager().getLoader(SingleProperty.ordinal()).onContentChanged();
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		getLoaderManager().initLoader(SingleProperty.ordinal(),
				ExtrasFactory.bundleFromProperty(getArgPropertyID()), new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		PropertyDTO property = PropertyDTO.fromCursor(cursor);
		super.onSingleRowLoaded(property);
		eventsListener.propertyLoaded(property);
	}

	@Override
	protected CharSequence getDetailsString(PropertyDTO entity) {
		return new DescriptionBuilder()
				.append("Property ID", entity.id, BuildConfig.DEBUG)
				.append("Property Name", entity.name)
				.append("Property Type", entity.type, BuildConfig.DEBUG)
				.append("# of rooms", entity.numDirectChildren)
				.append("# of rooms", entity.numAllChildren, BuildConfig.DEBUG)
				.append("# of items in the rooms", entity.numDirectItems)
				.append("# of items inside", entity.numAllItems)
				.append("image", entity.image, BuildConfig.DEBUG)
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
		new DeletePropertyTask(propertyID, new Dialogs.Callback() {
			public void dialogSuccess() {
				PropertyDTO item = new PropertyDTO();
				item.id = propertyID;
				eventsListener.propertyDeleted(item);
			}

			public void dialogFailed() {
				App.toast("Cannot delete property #" + propertyID);
			}
		}).displayDialog(getActivity());
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
