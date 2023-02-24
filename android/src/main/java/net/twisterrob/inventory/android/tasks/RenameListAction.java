package net.twisterrob.inventory.android.tasks;

import android.annotation.SuppressLint;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.ListDTO;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class RenameListAction extends RenameAction<ListDTO> {
	public RenameListAction(long listID) {
		super(listID, R.plurals.list);
	}

	@Override protected void doPrepare() {
		dto = retrieveList(id);
	}

	@SuppressLint("WrongThreadInterprocedural")
	@Override protected void doExecute() {
		String newName = getNewName(); // TODO this is UI thread access in Worker
		App.db().updateList(id, newName);
	}

	@Override public Action buildUndo() {
		return new UndoAction(this) {
			@Override public void execute() {
				App.db().updateList(id, dto.name);
			}
		};
	}
}
