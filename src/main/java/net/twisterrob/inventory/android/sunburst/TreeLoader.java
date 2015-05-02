package net.twisterrob.inventory.android.sunburst;

import java.util.*;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;

class TreeLoader {
	public void build(Node node) {
		int count = 0;
		int depth = -1;
		List<Node> children = loadChildren(node);
		for (Node child : children) {
			build(child);
			if (depth < child.depth) {
				depth = child.depth;
			}
			count += child.count;
		}
		node.depth = depth + 1;
		node.count = children.isEmpty()? 1 : count;
	}

	private List<Node> loadChildren(Node node) {
		switch (node.type) {
			case Root:
				node.children = loadProperties();
				break;
			case Property:
				node.children = loadRooms(node);
				break;
			case Item:
			case Room:
				node.children = loadItems(node);
				break;
			default:
				throw new IllegalStateException("Unknown type: " + node.type);
		}
		return node.children;
	}

	private static List<Node> loadProperties() {
		Cursor cursor = App.db().listProperties();
		try {
			List<Node> children = new ArrayList<>();
			while (cursor.moveToNext()) {
				long id = cursor.getLong(cursor.getColumnIndex(Property.ID));
				String label = cursor.getString(cursor.getColumnIndex(Property.NAME));
				children.add(new Node(Node.Type.Property, id, label));
			}
			return children;
		} finally {
			cursor.close();
		}
	}

	private static List<Node> loadRooms(Node node) {
		Cursor cursor = App.db().listRooms(node.id);
		try {
			List<Node> children = new ArrayList<>();
			while (cursor.moveToNext()) {
				long id = cursor.getLong(cursor.getColumnIndex(Room.ID));
				String label = cursor.getString(cursor.getColumnIndex(Room.NAME));
				children.add(new Node(Node.Type.Room, id, label));
			}
			return children;
		} finally {
			cursor.close();
		}
	}

	private static List<Node> loadItems(Node node) {
		Cursor cursor;
		if (node.type == Node.Type.Room) {
			cursor = App.db().listItemsInRoom(node.id);
		} else {
			cursor = App.db().listItems(node.id);
		}
		try {
			List<Node> children = new ArrayList<>();
			while (cursor.moveToNext()) {
				long id = cursor.getLong(cursor.getColumnIndex(Item.ID));
				String label = cursor.getString(cursor.getColumnIndex(Item.NAME));
				children.add(new Node(Node.Type.Item, id, label));
			}
			return children;
		} finally {
			cursor.close();
		}
	}
}
