package net.twisterrob.inventory.android.fragment.data;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.*;
import androidx.appcompat.view.ActionMode;

import net.twisterrob.android.utils.tools.ViewTools;
import net.twisterrob.android.view.SelectionAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity.Builder;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;
import net.twisterrob.inventory.android.tasks.*;
import net.twisterrob.inventory.android.view.*;

public class RoomListFragment extends BaseGalleryFragment<RoomsEvents> {
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

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int emptyText = getArgPropertyID() == Property.ID_ADD? R.string.room_empty_list : R.string.room_empty_child;
		listController = new BaseGalleryController(Loaders.Rooms, emptyText) {
			@Override public boolean canCreateNew() {
				return getArgPropertyID() != Property.ID_ADD;
			}

			@Override protected void onCreateNew() {
				eventsListener.newRoom(getArgPropertyID());
			}
		};
	}

	@Override
	public void onPrepareOptionsMenu(@NonNull Menu menu) {
		super.onPrepareOptionsMenu(menu);
		ViewTools.visibleIf(menu, R.id.action_room_add, listController.canCreateNew());
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_room_add:
				listController.createNew();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected SelectionActionMode onPrepareSelectionMode(@NonNull SelectionAdapter<?> adapter) {
		MoveTargetActivity.Builder builder = MoveTargetActivity
				.pick()
				.startFromPropertyList()
				.allowProperties()
				.forbidProperties(getArgPropertyID());
		return new RoomSelectionActionMode(this, adapter, builder);
	}

	@Override protected void onListItemLongClick(int position, long recyclerViewItemID) {
		eventsListener.roomActioned(recyclerViewItemID);
	}

	@Override protected void onListItemClick(int position, long recyclerViewItemID) {
		eventsListener.roomSelected(recyclerViewItemID);
	}

	@Override protected Bundle createLoadArgs() {
		return Intents.bundleFromProperty(getArgPropertyID());
	}

	private long getArgPropertyID() {
		return requireArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	public static RoomListFragment newInstance(long propertyID) {
		RoomListFragment fragment = new RoomListFragment();
		fragment.setArguments(Intents.bundleFromProperty(propertyID));
		return fragment;
	}
	public RoomListFragment addHeader() {
		setHeader(PropertyViewFragment.newInstance(getArgPropertyID()));
		return this;
	}

	private static class RoomSelectionActionMode extends SelectionActionMode {
		private final @NonNull BaseFragment<?> fragment;
		private final @NonNull Builder builder;

		public RoomSelectionActionMode(@NonNull BaseFragment<?> fragment, @NonNull SelectionAdapter<?> adapter, @NonNull Builder builder) {
			super(fragment.requireActivity(), adapter);
			this.fragment = fragment;
			this.builder = builder;
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
					Intent intent = builder.build();
					fragment.startActivityForResult(intent, PICK_REQUEST);
					return true;
			}
			return super.onActionItemClicked(mode, item);
		}

		@Override public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
			if (requestCode == PICK_REQUEST && resultCode == MoveTargetActivity.PROPERTY) {
				long propertyID = data.getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
				move(propertyID, getSelectedIDs());
				return true;
			}
			return false;
		}

		private void delete(final long... roomIDs) {
			Dialogs.executeConfirm(getActivity(), new DeleteRoomsAction(roomIDs) {
				public void finished() {
					finish();
					fragment.refresh();
				}
			});
		}

		private void move(final long propertyID, final long... roomIDs) {
			Dialogs.executeDirect(getActivity(), new MoveRoomsAction(propertyID, roomIDs) {
				public void finished() {
					finish();
					fragment.refresh();
				}
				@Override public void undoFinished() {
					fragment.refresh();
				}
			});
		}
	}
}
