package net.twisterrob.java.collections;

import java.util.*;

public class EmptyIterator<T> implements Iterator<T> {
	private static final Iterator<?> INSTANCE = new EmptyIterator<>();

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> Iterator<T> get() {
		return (Iterator)INSTANCE;
	}

	private EmptyIterator() {
		// prevent instantiation
	}

	@Override public boolean hasNext() {
		return false;
	}

	@Override public T next() {
		throw new NoSuchElementException();
	}

	@Override public void remove() {
		throw new UnsupportedOperationException();
	}
}
