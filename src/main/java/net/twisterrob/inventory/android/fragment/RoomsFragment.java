package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class RoomsFragment extends BaseFragment {
	private static final Logger LOG = LoggerFactory.getLogger(RoomsFragment.class);

	public interface RoomEvents {
		void newRoom();
		void roomSelected(long roomID, long rootItemID);
		void roomActioned(long roomID);
	}

	private CursorAdapter adapter;
	private GridView grid;
	private RoomEvents listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = checkActivityInterface(activity, RoomEvents.class);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.room_coll, container, false);

		grid = (GridView)root.findViewById(R.id.rooms);
		adapter = Adapters.loadCursorAdapter(getActivity(), R.xml.rooms, (Cursor)null);
		grid.setAdapter(adapter);

		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);

		grid.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				listener.roomActioned(id);
				return true;
			}
		});
		grid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				if (id == Room.ID_ADD) {
					listener.newRoom();
				} else {
					@SuppressWarnings("resource")
					Cursor data = (Cursor)parent.getAdapter().getItem(position);
					long rootItemID = data.getLong(data.getColumnIndexOrThrow(Room.ROOT_ITEM));
					listener.roomSelected(id, rootItemID);
				}
			}
		});
	}

	public void listForProperty(long id) {
		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, id);
		getLoaderManager().initLoader(Loaders.Rooms.ordinal(), args, new CursorSwapper(getActivity(), adapter));
	}

	public void refresh() {
		getLoaderManager().getLoader(Loaders.Rooms.ordinal()).forceLoad();
	}
}
