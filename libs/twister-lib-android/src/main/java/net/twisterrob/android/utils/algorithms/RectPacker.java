package net.twisterrob.android.utils.algorithms;

import java.util.Locale;

import android.graphics.Rect;

// http://www.blackpawn.com/texts/lightmaps/default.html
public class RectPacker<T> {
	public final Node<T> root = new Node<>();

	public RectPacker(Rect rect) {
		root.rect = rect;
	}

	public Node<T> insert(Rect bounds, T data) {
		Node<T> node = root.insert(bounds);
		if (node != null) {
			node.data = data;
		}
		return node;
	}

	public static class Node<T> {
		public Node<T> child1;
		public Node<T> child2;
		public Rect rect;
		public T data;

		Node<T> insert(Rect bounds) {
			if (!isLeaf()) {
				Node<T> newNode = child1.insert(bounds);
				if (newNode != null) {
					return newNode;
				}
				return child2.insert(bounds);
			} else {
				if (data != null) {
					return null;
				}

				Boolean fit = fit(rect, bounds);
				if (fit == null) {
					return this;
				}
				if (!fit) {
					return null;
				}

				child1 = new Node<>();
				child2 = new Node<>();

				int width = bounds.width();
				int height = bounds.width();
				int dw = rect.width() - width;
				int dh = rect.height() - height;

				if (dw > dh) {
					child1.rect = new Rect(rect.left, rect.top, rect.left + width, rect.bottom);
					child2.rect = new Rect(rect.left + width, rect.top, rect.right, rect.bottom);
				} else {
					child1.rect = new Rect(rect.left, rect.top, rect.right, rect.top + height);
					child2.rect = new Rect(rect.left, rect.top + height, rect.right, rect.bottom);
				}
				return child1.insert(bounds);
			}
		}

		/**
		 * @return false if img doesn't fit into rc
		 * true if img does fit into rc
		 * null if img perfectly fits into rc
		 */
		private static Boolean fit(Rect rc, Rect img) {
			if (rc.width() == img.width() && rc.height() == img.height()) {
				return null;
			} else if (rc.width() >= img.width() && rc.height() >= img.height()) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}

		private boolean isLeaf() {
			return child1 == null && child2 == null;
		}

		@Override
		public String toString() {
			return String.format(Locale.ROOT, "%s: %dx%d", rect, rect.width(), rect.height()); //NON-NLS
		}
	}
}
