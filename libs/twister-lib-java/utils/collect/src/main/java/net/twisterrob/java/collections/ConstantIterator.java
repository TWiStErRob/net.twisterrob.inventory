package net.twisterrob.java.collections;

import java.util.Iterator;

public class ConstantIterator<T> implements Iterator<T> {
	private final T entry;
	private boolean consumed;
	public ConstantIterator(T entry) {
		this.entry = entry;
	}

	@Override public boolean hasNext() {
		return !consumed;
	}

	@Override public T next() {
		consumed = true;
		return entry;
	}

	@Override public void remove() {
		throw new UnsupportedOperationException();
	}
}
