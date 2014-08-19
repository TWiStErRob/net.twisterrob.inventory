package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.data.PropertyEditActivity;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.PropertyViewFragment.PropertyEvents;
import net.twisterrob.inventory.android.tasks.DeletePropertyTask;
import net.twisterrob.inventory.android.utils.DescriptionBuilder;
import net.twisterrob.inventory.android.view.Dialogs;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyViewFragment extends BaseViewFragment<PropertyDTO, PropertyEvents> {
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
		getLoaderManager().getLoader(SingleProperty.ordinal()).forceLoad();
	}

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, getArgPropertyID());
		getLoaderManager().initLoader(SingleProperty.ordinal(), args, new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		PropertyDTO property = PropertyDTO.fromCursor(cursor);
		super.onSingleRowLoaded(property);
		eventsListener.propertyLoaded(property);
	}

	@Override
	protected CharSequence getDetailsString(PropertyDTO entity) {
		return new DescriptionBuilder() //
				.append("Property name", entity.name) //
				.append("# of rooms", entity.numDirectChildren) //
				.append("# of items in the rooms", entity.numDirectItems) //
				.append("# of items inside", entity.numAllItems) //
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

		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, propertyID);

		fragment.setArguments(args);
		return fragment;
	}
}
