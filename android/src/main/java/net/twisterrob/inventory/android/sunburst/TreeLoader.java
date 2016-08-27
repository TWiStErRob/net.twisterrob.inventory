package net.twisterrob.inventory.android.sunburst;

import android.database.Cursor;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.HierarchyBuilder;

class TreeLoader {
	private final NodeHierarchy hier = new NodeHierarchy();
	public Node build(Node root) {
		if (root.type != Node.Type.Root) {
			hier.preRegister(root.type.getType(), root.id, root);
		}
		populate(root);

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
				if (root.type == Node.Type.Root) {
					root.add(node);
				}
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

	private void populate(Node root) {
		Cursor rootBelonging = null;
		switch (root.type) {
			case Property:
				rootBelonging = App.db().getProperty(root.id);
				break;
			case Room:
				rootBelonging = App.db().getRoom(root.id);
				break;
			case Item:
				rootBelonging = App.db().getItem(root.id, false);
				break;
		}
		if (rootBelonging != null) {
			try {
				rootBelonging.moveToFirst();
				root.label = DatabaseTools.getString(rootBelonging, CommonColumns.NAME);
			} finally {
				rootBelonging.close();
			}
		}
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
		@Override protected void addPropertyChild(@NonNull Node parentProperty, @NonNull Node childRoom) {
			assert parentProperty.type == Node.Type.Property;
			assert childRoom.type == Node.Type.Room;
			parentProperty.add(childRoom);
		}
		@Override protected void addRoomChild(@NonNull Node parentRoom, @NonNull Node childItem) {
			assert parentRoom.type == Node.Type.Room;
			assert childItem.type == Node.Type.Item;
			parentRoom.add(childItem);
		}
		@Override protected void addItemChild(@NonNull Node parentItem, @NonNull Node childItem) {
			assert parentItem.type == Node.Type.Item;
			assert childItem.type == Node.Type.Item;
			parentItem.add(childItem);
		}

		@Override protected @NonNull Node createProperty(long id) {
			return new Node(Node.Type.Property, id);
		}
		@Override protected @NonNull Node createRoom(long id) {
			return new Node(Node.Type.Room, id);
		}
		@Override protected @NonNull Node createItem(long id) {
			return new Node(Node.Type.Item, id);
		}
	}
}
