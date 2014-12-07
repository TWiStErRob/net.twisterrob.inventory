package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.content.res.Resources;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class DeletePropertiesAction extends BaseAction {
	private final long[] propertyIDs;

	private Collection<String> properties;
	private final Collection<String> rooms = new TreeSet<>();

	public DeletePropertiesAction(long... propertyIDs) {
		this.propertyIDs = propertyIDs;
	}

	@Override public void prepare() {
		List<PropertyDTO> dtos = retrieveProperties(propertyIDs);
		properties = getNames(dtos);
		for (PropertyDTO property : dtos) {
			rooms.addAll(retrieveRoomNames(property.id));
		}
	}

	@Override public void execute() {
		for (long propertyID : propertyIDs) {
			App.db().deleteProperty(propertyID);
		}
	}

	@Override public String getConfirmationTitle(Resources res) {
		return quant(res, R.plurals.property_delete_title, properties);
	}

	@Override public String getConfirmationMessage(Resources res) {
		return buildConfirmString(res, properties, rooms,
				R.plurals.property_delete_confirm,
				R.plurals.property_delete_confirm_empty,
				R.plurals.property_delete_room_details
		);
	}

	@Override public String getSuccessMessage(Resources res) {
		return quant(res, R.plurals.property_delete_success, properties);
	}

	@Override public String getFailureMessage(Resources res) {
		return quant(res, R.plurals.property_delete_failed, properties);
	}

	@Override public Action buildUndo() {
		return null;
	}

	@Override public void undoFinished() {
		// no undo
	}
}
