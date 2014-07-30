package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.RoomsFragment.RoomEvents;

public class RoomsFragment extends BaseListFragment<RoomEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomsFragment.class);

	public interface RoomEvents {
		void newRoom();
		void roomSelected(long roomID, long rootItemID);
		void roomActioned(long roomID);
	}

	public RoomsFragment() {
		setDynamicResource(DYN_EventsClass, RoomEvents.class);
		setDynamicResource(DYN_Layout, R.layout.room_coll);
		setDynamicResource(DYN_List, R.id.rooms);
		setDynamicResource(DYN_CursorAdapter, R.xml.rooms);
		setDynamicResource(DYN_OptionsMenu, R.menu.rooms);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_room_add:
				eventsListener.newRoom();
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
				eventsListener.newRoom();
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
				long rootItemID = data.getLong(data.getColumnIndexOrThrow(Room.ROOT_ITEM));
				eventsListener.roomSelected(id, rootItemID);
			}
		});
	}

	public void listForProperty(long id) {
		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, id);
		getLoaderManager().initLoader(Loaders.Rooms.ordinal(), args, createListLoaderCallbacks());
	}

	public void refresh() {
		getLoaderManager().getLoader(Loaders.Rooms.ordinal()).forceLoad();
	}
}
