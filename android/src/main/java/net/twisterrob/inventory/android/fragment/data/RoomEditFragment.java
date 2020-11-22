package net.twisterrob.inventory.android.fragment.data;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;

import net.twisterrob.android.content.loader.DynamicLoaderManager;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.fragment.data.RoomEditFragment.RoomEditEvents;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class RoomEditFragment extends BaseEditFragment<RoomEditEvents, RoomDTO> {
	private static final Logger LOG = LoggerFactory.getLogger(RoomEditFragment.class);

	public interface RoomEditEvents {
		void roomLoaded(RoomDTO room);
		void roomSaved(long roomID);
	}

	public RoomEditFragment() {
		setDynamicResource(DYN_EventsClass, RoomEditEvents.class);
		setDynamicResource(DYN_NameHintResource, R.string.room_name_hint);
		setDynamicResource(DYN_DescriptionHintResource, R.string.room_description_hint);
		setKeepNameInSync(true);
	}

	protected @NonNull TypeAdapter createTypeAdapter() {
		TypeAdapter adapter = super.createTypeAdapter();
		adapter.setDisplayKeywords(true);
		return adapter;
	}

	@Override protected void onStartLoading() {
		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		Dependency<Cursor> populateTypes = manager.add(RoomTypes.id(), null, getTypeCallback());

		if (!isNew()) {
			Dependency<Cursor> loadRoomData = manager.add(SingleRoom.id(),
					Intents.bundleFromRoom(getArgRoomID()),
					SingleRoom.createCallbacks(requireContext(), new SingleRowLoaded())
			);
			loadRoomData.dependsOn(populateTypes); // type is auto-selected when a room is loaded
		}

		manager.startLoading();
	}

	@Override protected void onSingleRowLoaded(@NonNull Cursor cursor) {
		RoomDTO room = RoomDTO.fromCursor(cursor);
		onSingleRowLoaded(room);
		eventsListener.roomLoaded(room);
	}

	@Override protected @NonNull RoomDTO createDTO() {
		RoomDTO room = new RoomDTO();
		room.propertyID = getArgPropertyID();
		room.id = getArgRoomID();
		return room;
	}

	@Override protected boolean isNew() {
		return getArgRoomID() == Room.ID_ADD;
	}

	private long getArgPropertyID() {
		return requireArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	private long getArgRoomID() {
		return requireArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
	}

	@Override protected RoomDTO onSave(Database db, RoomDTO param) {
		if (param.id == Room.ID_ADD) {
			param.id = db.createRoom(param.propertyID, param.type, param.name, param.description);
		} else {
			db.updateRoom(param.id, param.type, param.name, param.description);
		}
		if (!param.hasImage) {
			// may clear already cleared images, but there's not enough info
			db.setRoomImage(param.id, null);
		} else if (param.image != null) {
			db.setRoomImage(param.id, db.addImage(param.image, null));
		} else {
			// it has an image, but there's no blob -> the image is already in DB
		}
		return param;
	}

	@Override protected void onSaved(@NonNull RoomDTO result) {
		eventsListener.roomSaved(result.id);
	}

	public static RoomEditFragment newInstance(long propertyID, long roomID) {
		if (propertyID == Property.ID_ADD && roomID == Room.ID_ADD) {
			throw new IllegalArgumentException("Property ID / room ID must be provided (new room / edit room)");
		}
		if (roomID != Room.ID_ADD) { // no need to know which property when editing
			propertyID = Property.ID_ADD;
		}

		RoomEditFragment fragment = new RoomEditFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, propertyID);
		args.putLong(Extras.ROOM_ID, roomID);

		fragment.setArguments(args);
		return fragment;
	}
}
