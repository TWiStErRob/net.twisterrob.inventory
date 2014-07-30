package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.fragment.PropertiesFragment.PropertyEvents;

public class PropertiesFragment extends BaseListFragment<PropertyEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesFragment.class);

	public interface PropertyEvents {
		void newProperty();
		void propertySelected(long propertyID);
		void propertyActioned(long propertyID);
	}

	public PropertiesFragment() {
		setDynamicResource(DYN_EventsClass, PropertyEvents.class);
		setDynamicResource(DYN_Layout, R.layout.property_coll);
		setDynamicResource(DYN_List, R.id.properties);
		setDynamicResource(DYN_CursorAdapter, R.xml.properties);
		setDynamicResource(DYN_OptionsMenu, R.menu.properties);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_add:
				eventsListener.newProperty();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);

		getView().findViewById(R.id.btn_add).setOnClickListener(new OnClickListener() {
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

	public void list() {
		getLoaderManager().initLoader(Loaders.Properties.ordinal(), null, createListLoaderCallbacks());
	}

	public void refresh() {
		getLoaderManager().getLoader(Loaders.Properties.ordinal()).forceLoad();
	}
}
