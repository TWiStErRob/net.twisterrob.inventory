package net.twisterrob.inventory.android.tasks;

import java.util.Collections;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.ListDTO;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class DeleteListAction extends DeleteAction {
	public DeleteListAction(long listID) {
		super(R.plurals.list, R.plurals.item, false, listID);
	}

	@Override protected void doPrepare() {
		super.doPrepare();
		ListDTO list = retrieveList(IDs[0]);
		targets = Collections.singleton(list.name);
		children.addAll(getNames(App.db().listItemsInList(list.id)));
	}

	@Override protected void doExecute() {
		for (long propertyID : IDs) {
			App.db().deleteList(propertyID);
		}
	}
}
