package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.activity.PropertyViewActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.fragment.PropertyListFragment.PropertiesEvents;

public class PropertyListFragment extends BaseListFragment<PropertiesEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(PropertyListFragment.class);

	public interface PropertiesEvents {
		void newProperty();
		void propertySelected(long propertyID);
		void propertyActioned(long propertyID);
	}

	public PropertyListFragment() {
		setDynamicResource(DYN_EventsClass, PropertiesEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.property_list);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_add:
				eventsListener.newProperty();
				return true;
			case R.id.action_room_list:
				startActivity(PropertyViewActivity.show(Property.ID_ADD));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);

		view.findViewById(R.id.btn_add).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				eventsListener.newProperty();
			}
		});

		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				eventsListener.propertyActioned(id);
				return true;
			}
		});
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				eventsListener.propertySelected(id);
			}
		});
	}

	@Override
	protected void onStartLoading() {
		getLoaderManager().initLoader(Loaders.Properties.ordinal(), null, createListLoaderCallbacks());
	}

	@Override
	protected void onRefresh() {
		getLoaderManager().getLoader(Loaders.Properties.ordinal()).forceLoad();
	}

	public static PropertyListFragment newInstance() {
		PropertyListFragment fragment = new PropertyListFragment();
		return fragment;
	}
}
