package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;

public class RoomListFragment extends BaseListFragment<RoomsEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomListFragment.class);

	public interface RoomsEvents {
		void newRoom(long propertyID);
		void roomSelected(RoomDTO room);
		void roomActioned(long roomID);
	}

	public RoomListFragment() {
		setDynamicResource(DYN_EventsClass, RoomsEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.room_list);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.findItem(R.id.action_room_add).setVisible(getArgPropertyID() != Property.ID_ADD);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_room_add:
				eventsListener.newRoom(getArgPropertyID());
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
				eventsListener.newRoom(getArgPropertyID());
			}
		});

		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				eventsListener.roomActioned(id);
				return true;
			}
		});
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				@SuppressWarnings("resource")
				Cursor data = (Cursor)parent.getAdapter().getItem(position);
				eventsListener.roomSelected(RoomDTO.fromCursor(data));
			}
		});
	}

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, getArgPropertyID());
		getLoaderManager().initLoader(Loaders.Rooms.ordinal(), args, createListLoaderCallbacks());
	}

	@Override
	protected void onRefresh() {
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
