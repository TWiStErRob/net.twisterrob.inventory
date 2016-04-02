package net.twisterrob.inventory.android.sunburst;

import java.util.*;

class Node {
	final Type type;
	final long id;
	String label;
	int count;
	int filteredCount;
	int depth;
	Node parent;
	final List<Node> children = new ArrayList<>();

	public Node(Type type, long id) {
		this.type = type;
		this.id = id;
	}
	public void add(Node node) {
		node.parent = this;
		children.add(node);
	}
	public int getCount() {
		return count - filteredCount;
	}

	enum Type {
		Root(null),
		Property(net.twisterrob.inventory.android.content.contract.Type.Property),
		Room(net.twisterrob.inventory.android.content.contract.Type.Room),
		Item(net.twisterrob.inventory.android.content.contract.Type.Item);

		private final net.twisterrob.inventory.android.content.contract.Type type;

		Type(net.twisterrob.inventory.android.content.contract.Type type) {
			this.type = type;
		}

		public net.twisterrob.inventory.android.content.contract.Type getType() {
			return type;
		}
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "%s #%d: %s", type, id, label);
	}
}
