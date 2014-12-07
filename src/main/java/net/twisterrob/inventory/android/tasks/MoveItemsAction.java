package net.twisterrob.inventory.android.tasks;

import java.util.List;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class MoveItemsAction extends MoveAction {
	private List<ItemDTO> items;

	public MoveItemsAction(long parentID, long... itemIDs) {
		super(parentID, R.plurals.item, R.plurals.item, itemIDs);
	}

	@Override protected void doPrepare() {
		super.doPrepare();
		items = retrieveItems(IDs);
		stuff = getNames(items);
		target = retrieveItem(targetID).name;
	}

	@Override protected void doExecute() {
		App.db().moveItems(targetID, IDs);
	}

	@Override public Action buildUndo() {
		return new UndoAction(this) {
			@Override public void execute() {
				for (ItemDTO item : items) {
					App.db().moveItem(item.id, item.parentID);
				}
			}
		};
	}
}
