package net.twisterrob.inventory.android.tasks;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.Item;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.view.Action;

public abstract class DeleteItemTask implements Action {
	private final long itemID;

	private ItemDTO item;
	private List<String> items;

	public DeleteItemTask(long id) {
		this.itemID = id;
	}

	@Override public void prepare() {
		item = retrieveItem();
		items = retrieveItemNames();
	}

	@Override public void execute() {
		App.db().deleteItem(itemID);
	}

	@Override public String getConfirmationTitle() {
		return "Deleting Item #" + itemID;
	}

	@Override public String getConfirmationMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Are you sure you want to delete the item named");
		sb.append(' ');
		sb.append("'").append(item.name).append("'");
		if (!items.isEmpty()) {
			sb.append(" and all ");
			sb.append(items.size());
			sb.append(" items with all items in it");
		}
		sb.append("?");
		if (!items.isEmpty()) {
			sb.append("\n(The items are: ");
			for (String name : items) {
				sb.append(name);
				sb.append(", ");
			}
			sb.delete(sb.length() - ", ".length(), sb.length());
			sb.append(")");
		}
		return sb.toString();
	}

	@Override public String getSuccessMessage() {
		return "Item #" + itemID + " deleted.";
	}

	@Override public String getFailureMessage() {
		return "Cannot delete item #" + itemID + ".";
	}

	@Override public Action buildUndo() {
		return null;
	}

	private ItemDTO retrieveItem() {
		Cursor item = App.db().getItem(itemID);
		try {
			item.moveToFirst();
			return ItemDTO.fromCursor(item);
		} finally {
			item.close();
		}
	}

	private List<String> retrieveItemNames() {
		Cursor items = App.db().listItems(item.id);
		try {
			List<String> itemNames = new ArrayList<>(items.getCount());
			while (items.moveToNext()) {
				itemNames.add(items.getString(items.getColumnIndexOrThrow(Item.NAME)));
			}
			return itemNames;
		} finally {
			items.close();
		}
	}
}