package net.twisterrob.inventory.android.sunburst;

import android.database.Cursor;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.HierarchyBuilder;

class TreeLoader {
	private final NodeHierarchy hier = new NodeHierarchy();
	public Node build(Node root) {
		Type rootType = root.type.getType();
		if (rootType != null) {
			hier.preRegister(rootType, root.id, root);
		}

		Cursor cursor = App.db().subtree(
				root.type == Node.Type.Property? root.id : null,
				root.type == Node.Type.Room? root.id : null,
				root.type == Node.Type.Item? root.id : null
		);
		while (cursor.moveToNext()) {
			long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
			String name = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
			Type type = Type.from(cursor, CommonColumns.TYPE);
			Node node = hier.getOrCreate(type, id);
			node.label = name;

			if (type == Type.Property) {
				root.add(node);
			} else {
				Type parentType = Type.from(cursor, ParentColumns.PARENT_TYPE);
				long parentID = cursor.getLong(cursor.getColumnIndexOrThrow(ParentColumns.PARENT_ID));
				hier.put(parentType, parentID, node);
			}
		}
		cursor.close();

		decorate(root);
		return root;
	}

	private void decorate(Node node) {
		int depth = -1;
		int count = 0;
		for (Node child : node.children) {
			decorate(child);
			if (depth < child.depth) {
				depth = child.depth;
			}
			count += child.count;
		}
		node.count = node.children.isEmpty()? 1 : count;
		node.depth = depth + 1;
	}

	private static class NodeHierarchy extends HierarchyBuilder<Node, Node, Node, Node> {
		@Override protected void addPropertyChild(Node parentProperty, Node childRoom) {
			parentProperty.add(childRoom);
		}
		@Override protected void addRoomChild(Node parentRoom, Node childItem) {
			parentRoom.add(childItem);
		}
		@Override protected void addItemChild(Node parentItem, Node childItem) {
			parentItem.add(childItem);
		}

		@Override protected Node createProperty(long id) {
			return new Node(Node.Type.Property, id);
		}
		@Override protected Node createRoom(long id) {
			return new Node(Node.Type.Room, id);
		}
		@Override protected Node createItem(long id) {
			return new Node(Node.Type.Item, id);
		}
	}
}
