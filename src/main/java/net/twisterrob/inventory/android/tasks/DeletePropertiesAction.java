package net.twisterrob.inventory.android.tasks;

import java.util.Collection;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class DeletePropertiesAction extends BaseAction {
	private final long propertyID;

	private PropertyDTO property;
	private Collection<String> rooms;

	public DeletePropertiesAction(long id) {
		this.propertyID = id;
	}

	@Override public void prepare() {
		property = retrieveProperty(propertyID);
		rooms = retrieveRoomNames(propertyID);
	}

	@Override public void execute() {
		App.db().deleteProperty(propertyID);
	}

	@Override public String getConfirmationTitle() {
		return "Deleting Property #" + propertyID;
	}

	@Override public String getConfirmationMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Are you sure you want to delete the property named");
		sb.append(' ');
		sb.append("'").append(property.name).append("'");
		if (!rooms.isEmpty()) {
			sb.append(" and all ");
			sb.append(rooms.size());
			sb.append(" rooms with all items in it");
		}
		sb.append("?");
		if (!rooms.isEmpty()) {
			sb.append("\n(The rooms are: ");
			for (String name : rooms) {
				sb.append(name);
				sb.append(", ");
			}
			sb.delete(sb.length() - ", ".length(), sb.length());
			sb.append(")");
		}
		return sb.toString();
	}

	@Override public String getSuccessMessage() {
		return "Property #" + propertyID + "deleted.";
	}

	@Override public String getFailureMessage() {
		return "Cannot delete property #" + propertyID + ".";
	}

	@Override public Action buildUndo() {
		return null;
	}

	@Override public void undoFinished() {
		// optional override
	}
}