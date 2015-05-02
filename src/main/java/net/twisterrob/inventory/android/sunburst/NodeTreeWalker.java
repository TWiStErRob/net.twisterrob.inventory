package net.twisterrob.inventory.android.sunburst;

import net.twisterrob.android.graphics.SunburstDrawable;

class NodeTreeWalker implements SunburstDrawable.TreeWalker<Node> {
	@Override public Iterable<Node> getChildren(Node node, int level) {
		return node.children;
	}

	@Override public int getDepth(Node subTree) {
		return subTree.depth;
	}

	@Override public String getLabel(Node node, int level) {
		return node.label;
	}

	@Override public float getWeight(Node subTree, int level) {
		return subTree.count;
	}
}
