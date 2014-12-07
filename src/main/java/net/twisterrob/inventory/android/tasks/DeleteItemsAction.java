package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.content.res.Resources;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.view.Action;

import static net.twisterrob.inventory.android.content.DatabaseDTOTools.*;

public abstract class DeleteItemsAction extends BaseAction {
	private final long[] itemIDs;

	private Collection<String> items;
	private final Collection<String> children = new TreeSet<>();

	public DeleteItemsAction(long... itemIDs) {
		this.itemIDs = itemIDs;
	}

	@Override public void prepare() {
		List<ItemDTO> dtos = retrieveItems(itemIDs);
		items = getNames(dtos);
		for (ItemDTO dto : dtos) {
			children.addAll(retrieveItemNames(dto.id));
		}
	}

	@Override public void execute() {
		for (long itemID : itemIDs) {
			App.db().deleteItem(itemID);
		}
	}

	@Override public String getConfirmationTitle(Resources res) {
		return quant(res, R.plurals.item_delete_title, items);
	}

	@Override public String getConfirmationMessage(Resources res) {
		return buildConfirmString(res, items, children,
				R.plurals.item_delete_confirm,
				R.plurals.item_delete_confirm_empty,
				R.plurals.item_delete_item_details
		);
	}

	@Override public String getSuccessMessage(Resources res) {
		return quant(res, R.plurals.item_delete_success, items);
	}

	@Override public String getFailureMessage(Resources res) {
		return quant(res, R.plurals.item_delete_failed, items);
	}

	@Override public Action buildUndo() {
		return null;
	}

	@Override public void undoFinished() {
		// no undo
	}
}
