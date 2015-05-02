package net.twisterrob.inventory.android.sunburst;

import java.util.*;

class Node {
	final Type type;
	final long id;
	final String label;
	int count;
	int depth;
	List<Node> children;

	public Node(Type type, long id, String label) {
		this.type = type;
		this.id = id;
		this.label = label;
	}

	enum Type {
		Root,
		Property,
		Room,
		Item
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "%s #%d: %s", type, id, label);
	}
}
