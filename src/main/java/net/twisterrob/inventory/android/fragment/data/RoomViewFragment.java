package net.twisterrob.inventory.android.fragment.data;

import java.util.Date;

import org.slf4j.*;

import android.content.Intent;
import android.database.Cursor;
import android.view.MenuItem;

import net.twisterrob.android.utils.tools.TextTools.DescriptionBuilder;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.fragment.data.RoomViewFragment.RoomEvents;
import net.twisterrob.inventory.android.sunburst.SunburstActivity;
import net.twisterrob.inventory.android.tasks.*;
import net.twisterrob.inventory.android.view.*;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class RoomViewFragment extends BaseViewFragment<RoomDTO, RoomEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomViewFragment.class);

	private static final int MOVE_REQUEST = 0;

	public interface RoomEvents {
		void roomLoaded(RoomDTO room);
		void roomDeleted(RoomDTO room);
		void roomMoved(long roomID, long newPropertyID);
	}

	private long propertyID = Property.ID_ADD;

	public RoomViewFragment() {
		setDynamicResource(DYN_EventsClass, RoomEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.room);
	}

	@Override
	protected void onRefresh() {
		super.onRefresh();
		getLoaderManager().getLoader(SingleRoom.id()).onContentChanged();
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		getLoaderManager().initLoader(SingleRoom.id(),
				Intents.bundleFromRoom(getArgRoomID()), new SingleRowLoaded());
	}

	@Override
	protected void onSingleRowLoaded(Cursor cursor) {
		RoomDTO room = RoomDTO.fromCursor(cursor);
		propertyID = room.propertyID;
		super.onSingleRowLoaded(room);
		eventsListener.roomLoaded(room);
	}

	@Override
	protected CharSequence getDetailsString(RoomDTO entity, boolean DEBUG) {
		return new DescriptionBuilder()
				.append("Room ID", entity.id, DEBUG)
				.append("Room Name", entity.name)
				.append("Room Type", entity.type, DEBUG)
				.append("Room Root", entity.rootItemID, DEBUG)
				.append("Property ID", entity.propertyID, DEBUG)
				.append("In property", entity.propertyName)
				.append("# of items in the room", entity.numDirectItems)
				.append("# of items inside items", entity.numAllItems)
				.append(entity.hasImage? "image" : "image removed", new Date(entity.imageTime), DEBUG)
				.append("Description", entity.description)
				.build();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_room_edit:
				startActivity(RoomEditActivity.edit(getArgRoomID()));
				return true;
			case R.id.action_room_delete:
				delete(getArgRoomID());
				return true;
			case R.id.action_room_move:
				Intent intent = MoveTargetActivity.pick()
				                                  .startFromPropertyList()
				                                  .allowProperties()
				                                  .forbidProperties(propertyID)
				                                  .build();
				startActivityForResult(intent, MOVE_REQUEST);
				return true;
			case R.id.action_share:

				return true;
			case R.id.action_room_sunburst:
				startActivity(SunburstActivity.displayRoom(getArgRoomID()));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MOVE_REQUEST && resultCode == MoveTargetActivity.PROPERTY) {
			long propertyID = data.getLongExtra(Extras.PROPERTY_ID, Property.ID_ADD);
			move(getArgRoomID(), propertyID);
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void move(final long roomID, final long propertyID) {
		Dialogs.executeConfirm(getActivity(), new MoveRoomsAction(propertyID, roomID) {
			@Override public void finished() {
				eventsListener.roomMoved(roomID, propertyID);
			}
			@Override public Action buildUndo() {
				// we navigated away from current activity, no undo
				return null;
			}
			@Override public void undoFinished() {
				// no undo
			}
		});
	}

	private void delete(final long roomID) {
		Dialogs.executeConfirm(getActivity(), new DeleteRoomsAction(roomID) {
			@Override public void finished() {
				RoomDTO room = new RoomDTO();
				room.id = roomID;
				eventsListener.roomDeleted(room);
			}
		});
	}

	@Override protected void editImage() {
		startActivity(BaseEditActivity.takeImage(RoomEditActivity.edit(getArgRoomID())));
	}

	private long getArgRoomID() {
		return getArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
	}

	public static RoomViewFragment newInstance(long roomID) {
		if (roomID == Room.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing room");
		}

		RoomViewFragment fragment = new RoomViewFragment();
		fragment.setArguments(Intents.bundleFromRoom(roomID));
		return fragment;
	}
}
