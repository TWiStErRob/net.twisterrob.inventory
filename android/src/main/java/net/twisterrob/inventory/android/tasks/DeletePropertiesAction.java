package net.twisterrob.inventory.android.tasks;

import java.util.List;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class DeletePropertiesAction extends DeleteAction {
	public DeletePropertiesAction(long... propertyIDs) {
		super(R.plurals.property, R.plurals.room, propertyIDs);
	}

	@Override protected void doPrepare() {
		super.doPrepare();
		List<PropertyDTO> dtos = retrieveProperties(IDs);
		targets = getNames(dtos);
		for (PropertyDTO property : dtos) {
			children.addAll(retrieveRoomNames(property.id));
		}
	}

	@Override protected void doExecute() {
		for (long propertyID : IDs) {
			App.db().deleteProperty(propertyID);
		}
	}
}
