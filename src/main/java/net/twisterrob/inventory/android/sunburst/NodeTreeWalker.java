package net.twisterrob.inventory.android.sunburst;

import java.util.*;

import net.twisterrob.android.graphics.SunburstDrawable;
import net.twisterrob.java.collections.FilteredIterator;
import net.twisterrob.java.collections.FilteredIterator.Filter;

class NodeTreeWalker implements SunburstDrawable.TreeWalker<Node> {
	public final Set<Node> ignore = new HashSet<>();
	private final int ignoreBelowLevel;

	NodeTreeWalker(int ignoreBelowLevel) {
		this.ignoreBelowLevel = ignoreBelowLevel;
	}

	@Override public Iterable<Node> getChildren(final Node node, final int level) {
		return new Iterable<Node>() {
			@Override public Iterator<Node> iterator() {
				return new FilteredIterator<>(node.children.iterator(), new Filter<Node>() {
					@Override public boolean matches(Node element) {
						return !(element.depth == 0 && ignoreBelowLevel <= level) && !ignore.contains(element);
					}
				});
			}
		};
	}

	@Override public int getDepth(Node subTree) {
		return subTree.depth;
	}

	@Override public String getLabel(Node node, int level) {
		return node.label;
	}

	@Override public float getWeight(Node subTree, int level) {
		return subTree.getCount();
	}
}
