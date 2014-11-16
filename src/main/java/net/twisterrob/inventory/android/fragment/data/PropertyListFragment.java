package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.PropertyViewActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.fragment.data.PropertyListFragment.PropertiesEvents;

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_add:
				onCreateNew();
				return true;
			case R.id.action_room_list:
				startActivity(PropertyViewActivity.show(Property.ID_ADD));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected boolean canCreateNew() {
		return true;
	}

	@Override protected void onCreateNew() {
		eventsListener.newProperty();
	}

	@Override public void onItemClick(RecyclerView.ViewHolder holder) {
		eventsListener.propertySelected(holder.getItemId());
	}

	@Override public boolean onItemLongClick(RecyclerView.ViewHolder holder) {
		eventsListener.propertyActioned(holder.getItemId());
		return true;
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
