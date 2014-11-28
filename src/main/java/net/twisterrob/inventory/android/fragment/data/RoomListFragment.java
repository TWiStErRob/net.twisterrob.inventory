package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;

public class RoomListFragment extends BaseGalleryFragment<RoomsEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomListFragment.class);

	public interface RoomsEvents {
		void newRoom(long propertyID);
		void roomSelected(long roomID);
		void roomActioned(long roomID);
	}

	public RoomListFragment() {
		setDynamicResource(DYN_EventsClass, RoomsEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.room_list);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_room_add).setVisible(canCreateNew());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_room_add:
				onCreateNew();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected boolean canCreateNew() {
		return getArgPropertyID() != Property.ID_ADD;
	}

	@Override public void onCreateNew() {
		eventsListener.newRoom(getArgPropertyID());
	}

	@Override protected void onListItemLongClick(RecyclerView.ViewHolder holder) {
		eventsListener.roomActioned(holder.getItemId());
	}

	@Override protected void onListItemClick(RecyclerView.ViewHolder holder) {
		eventsListener.roomSelected(holder.getItemId());
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, getArgPropertyID());
		getLoaderManager().initLoader(Loaders.Rooms.ordinal(), args, createListLoaderCallbacks());
	}

	@Override
	protected void onRefresh() {
		super.onRefresh();
		getLoaderManager().getLoader(Loaders.Rooms.ordinal()).forceLoad();
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static RoomListFragment newInstance(long propertyID) {
		RoomListFragment fragment = new RoomListFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, propertyID);

		fragment.setArguments(args);
		return fragment;
	}
}
