package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.PropertyViewActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.fragment.data.PropertyListFragment.PropertiesEvents;
import net.twisterrob.inventory.android.view.RecyclerViewLoadersController;

public class PropertyListFragment extends BaseGalleryFragment<PropertiesEvents> {
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

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new RecyclerViewLoadersController(this, Loaders.Properties) {
			@Override protected CursorRecyclerAdapter setupList() {
				return PropertyListFragment.super.setupList(list);
			}
			@Override public boolean canCreateNew() {
				return true;
			}
			@Override protected void onCreateNew() {
				eventsListener.newProperty();
			}
		};
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_add:
				listController.createNew();
				return true;
			case R.id.action_room_list:
				startActivity(PropertyViewActivity.show(Property.ID_ADD));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected void onListItemClick(RecyclerView.ViewHolder holder) {
		eventsListener.propertySelected(holder.getItemId());
	}

	@Override protected void onListItemLongClick(RecyclerView.ViewHolder holder) {
		eventsListener.propertyActioned(holder.getItemId());
	}

	public static PropertyListFragment newInstance() {
		PropertyListFragment fragment = new PropertyListFragment();
		return fragment;
	}
}
