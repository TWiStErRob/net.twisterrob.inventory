package net.twisterrob.inventory.android.tasks;

import java.util.List;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class DeleteItemsAction extends DeleteAction {
	public DeleteItemsAction(long... itemIDs) {
		super(R.plurals.item, R.plurals.item, itemIDs);
	}

	@Override protected void doPrepare() {
		super.doPrepare();
		List<ItemDTO> dtos = retrieveItems(IDs);
		targets = getNames(dtos);
		for (ItemDTO dto : dtos) {
			children.addAll(retrieveItemNames(dto.id));
		}
	}

	@Override protected void doExecute() {
		for (long itemID : IDs) {
			App.db().deleteItem(itemID);
		}
	}
}
