package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.content.res.Resources;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.RoomDTO;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class DeleteRoomsAction extends BaseAction {
	private final long[] roomIDs;

	private Collection<String> rooms;
	private final Collection<String> items = new TreeSet<>();

	public DeleteRoomsAction(long... roomIDs) {
		if (roomIDs.length == 0) {
			throw new IllegalArgumentException("Nothing to move.");
		}
		this.roomIDs = roomIDs;
	}

	@Override public void prepare() {
		List<RoomDTO> dtos = retrieveRooms(roomIDs);
		rooms = getNames(dtos);
		for (RoomDTO room : dtos) {
			items.addAll(retrieveItemNames(room.rootItemID));
		}
	}

	@Override public void execute() {
		for (long roomID : roomIDs) {
			App.db().deleteRoom(roomID);
		}
	}

	@Override public String getConfirmationTitle(Resources res) {
		return quant(res, R.plurals.room_delete_title, rooms);
	}

	@Override public String getConfirmationMessage(Resources res) {
		return buildConfirmString(res, rooms, items,
				R.plurals.room_delete_confirm,
				R.plurals.room_delete_confirm_empty,
				R.plurals.room_delete_room_details
		);
	}

	@Override public String getSuccessMessage(Resources res) {
		return quant(res, R.plurals.room_delete_success, rooms);
	}

	@Override public String getFailureMessage(Resources res) {
		return quant(res, R.plurals.room_delete_failed, rooms);
	}

	@Override public Action buildUndo() {
		return null;
	}

	@Override public void undoFinished() {
		// no undo
	}
}
