package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;
import net.twisterrob.inventory.android.tasks.*;
import net.twisterrob.inventory.android.view.*;

public class RoomListFragment extends BaseGalleryFragment<RoomsEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomListFragment.class);
	private static final int PICK_REQUEST = 1;

	public interface RoomsEvents {
		void newRoom(long propertyID);
		void roomSelected(long roomID);
		void roomActioned(long roomID);
	}

	public RoomListFragment() {
		setDynamicResource(DYN_EventsClass, RoomsEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.room_list);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new RecyclerViewLoadersController(this, Loaders.Rooms) {
			@Override protected CursorRecyclerAdapter setupList() {
				return RoomListFragment.super.setupList(list);
			}

			@Override public boolean canCreateNew() {
				return getArgPropertyID() != Property.ID_ADD;
			}

			@Override protected void onCreateNew() {
				eventsListener.newRoom(getArgPropertyID());
			}
		};
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_room_add).setVisible(listController.canCreateNew());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_room_add:
				listController.createNew();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected SelectionActionMode onPrepareSelectionMode(Activity activity, SelectionAdapter<?> adapter) {
		return new SelectionActionMode(activity, adapter) {
			@Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.room_bulk, menu);
				return super.onCreateActionMode(mode, menu);
			}

			@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
					case R.id.action_room_delete:
						delete(selectionMode.getSelectedIDs());
						return true;
					case R.id.action_room_move:
						Intent intent = MoveTargetActivity.pick()
						                                  .startFromPropertyList()
						                                  .allowProperties()
						                                  .forbidProperties(getArgPropertyID())
						                                  .build();
						startActivityForResult(intent, PICK_REQUEST);
						return true;
				}
				return super.onActionItemClicked(mode, item);
			}
		};
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_REQUEST && resultCode == MoveTargetActivity.PROPERTY) {
			long propertyID = data.getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
			move(propertyID, selectionMode.getSelectedIDs());
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override protected void onListItemLongClick(int position, long recyclerViewItemID) {
		eventsListener.roomActioned(recyclerViewItemID);
	}

	@Override protected void onListItemClick(int position, long recyclerViewItemID) {
		eventsListener.roomSelected(recyclerViewItemID);
	}

	@Override protected Bundle createLoadArgs() {
		return ExtrasFactory.bundleFromProperty(getArgPropertyID());
	}

	private void delete(final long... roomIDs) {
		Dialogs.executeConfirm(getActivity(), new DeleteRoomsAction(roomIDs) {
			public void finished() {
				selectionMode.finish();
				refresh();
			}
		});
	}

	private void move(final long propertyID, final long... roomIDs) {
		Dialogs.executeDirect(getActivity(), new MoveRoomsAction(propertyID, roomIDs) {
			public void finished() {
				selectionMode.finish();
				refresh();
			}
			@Override public void undoFinished() {
				refresh();
			}
		});
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static RoomListFragment newInstance(long propertyID) {
		RoomListFragment fragment = new RoomListFragment();
		fragment.setArguments(ExtrasFactory.bundleFromProperty(propertyID));
		return fragment;
	}
	public RoomListFragment addHeader() {
		setHeader(PropertyViewFragment.newInstance(getArgPropertyID()));
		return this;
	}
}
