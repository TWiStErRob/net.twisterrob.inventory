package net.twisterrob.inventory.android.fragment.data;

import java.util.Arrays;

import org.slf4j.*;

import android.content.Intent;
import android.os.*;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;
import net.twisterrob.inventory.android.tasks.*;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.Dialogs.Callback;
import net.twisterrob.inventory.android.view.UndobarController.UndoListener;

public class RoomListFragment extends BaseGalleryFragment<RoomsEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomListFragment.class);
	private UndobarController controller;
	private UndoListener undoMove = new UndoListener() {
		@Override public void onUndo(Parcelable token) {
			new MoveRoomTask(new Callback() {
				@Override public void dialogSuccess() {
					App.toast("Undo'd");
					refresh();
				}
				@Override public void dialogFailed() {
					App.toast("Undo failed");
				}
			}, getArgPropertyID(), ExtrasFactory.getIDsFrom((Bundle)token)).executeBackground();
		}
	};

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

	@Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.room_bulk, menu);
		return super.onCreateActionMode(mode, menu);
	}

	@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_room_delete:
				delete(getSelectedIDs());
				return true;
			case R.id.action_room_move:
				startActivityForResult(MoveTargetActivity.pick(MoveTargetActivity.PROPERTY), PICK_REQUEST);
				return true;
		}
		return super.onActionItemClicked(mode, item);
	}

	public static final int PICK_REQUEST = 1;

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_REQUEST && resultCode == MoveTargetActivity.PROPERTY) {
			long propertyID = data.getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
			move(propertyID, getSelectedIDs());
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
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

	@Override protected void onListItemLongClick(RecyclerView.ViewHolder holder) {
		eventsListener.roomActioned(holder.getItemId());
	}

	@Override protected void onListItemClick(RecyclerView.ViewHolder holder) {
		eventsListener.roomSelected(holder.getItemId());
	}

	@Override protected Bundle createLoadArgs() {
		return ExtrasFactory.bundleFromProperty(getArgPropertyID());
	}

	private void delete(final long[] roomIDs) {
		new DeleteRoomTask(new Dialogs.Callback() {
			public void dialogSuccess() {
				finishActionMode();
				refresh();
			}

			public void dialogFailed() {
				App.toast("Cannot delete rooms: " + Arrays.toString(roomIDs));
			}
		}, roomIDs).displayDialog(getActivity());
	}
	private void move(final long propertyID, final long[] roomIDs) {
		if (propertyID == getArgPropertyID()) {
			App.toast("Cannot move rooms to the same property where they are.");
			return;
		}
		new MoveRoomTask(new Dialogs.Callback() {
			public void dialogFailed() {
				App.toast("Cannot move " + Arrays.toString(roomIDs) + " to property #" + propertyID);
			}
			public void dialogSuccess() {
				finishActionMode();
				refresh();
				String message = getResources().getQuantityString(R.plurals.room_moved, roomIDs.length, roomIDs.length);
				showUndo(message, undoMove, ExtrasFactory.bundleFromIDs(roomIDs));
			}
		}, propertyID, roomIDs).executeBackground();
	}
	private void showUndo(String message, UndoListener listener, Bundle listenerArgs) {
		if (controller == null) {
			controller = new UndobarController(getActivity());
		}
		controller.showUndoBar(false, message, listenerArgs, listener);
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static RoomListFragment newInstance(long propertyID) {
		RoomListFragment fragment = new RoomListFragment();
		fragment.setArguments(ExtrasFactory.bundleFromProperty(propertyID));
		return fragment;
	}
}
