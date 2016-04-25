package net.twisterrob.java.collections;

import java.util.Iterator;

public class PerpetualIterator<T> implements Iterator<T> {
	private final Iterable<T> provider;
	private Iterator<T> current = EmptyIterator.get();

	public PerpetualIterator(Iterable<T> provider) {
		this.provider = provider;
	}

	@Override public boolean hasNext() {
		if (!current.hasNext()) {
			current = provider.iterator();
		}
		return current.hasNext();
	}

	@Override public T next() {
		return current.next();
	}

	@Override public void remove() {
		current.remove();
	}
}
