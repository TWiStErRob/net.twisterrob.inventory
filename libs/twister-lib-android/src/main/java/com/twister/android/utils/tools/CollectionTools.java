package com.twister.android.utils.tools;

import java.io.*;
import java.util.*;

public final class CollectionTools {
	private CollectionTools() {
		// prevent instantiation
	}

	public static void ensureIndexValid(final List<? extends Object> list, int i) {
		i -= list.size();
		while (i-- >= 0) {
			list.add(null);
		}
	}

	public static <T> T coalesce(final T... objects) {
		for (T t: objects) {
			if (t != null) {
				return t;
			}
		}
		return null;
	}

	public static <T> List<T> remove(final List<T> list, final Class<? extends T> clazz) {
		List<T> removed = new LinkedList<T>();
		Iterator<T> it = list.listIterator();
		while (it.hasNext()) {
			T item = it.next();
			if (clazz.isAssignableFrom(item.getClass())) {
				it.remove();
				removed.add(item);
			}
		}
		return removed;
	}

	/**
	 * Returns a set backed by {@code map}.
	 * 
	 * @throws IllegalArgumentException if the map is not empty
	 * @since 1.6
	 */
	public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
		if (map.isEmpty()) {
			return new SetFromMap<E>(map);
		}
		throw new IllegalArgumentException();
	}

	private static class SetFromMap<E> extends AbstractSet<E> implements Serializable {
		private static final long serialVersionUID = 2454657854757543876L;

		// must named as it, to pass serialization compatibility test.
		private Map<E, Boolean> m;

		private transient Set<E> backingSet;

		SetFromMap(final Map<E, Boolean> map) {
			m = map;
			backingSet = map.keySet();
		}

		@Override
		public boolean equals(Object object) {
			return backingSet.equals(object);
		}

		@Override
		public int hashCode() {
			return backingSet.hashCode();
		}

		@Override
		public boolean add(E object) {
			return m.put(object, Boolean.TRUE) == null;
		}

		@Override
		public void clear() {
			m.clear();
		}

		@Override
		public String toString() {
			return backingSet.toString();
		}

		@Override
		public boolean contains(Object object) {
			return backingSet.contains(object);
		}

		@Override
		public boolean containsAll(Collection<?> collection) {
			return backingSet.containsAll(collection);
		}

		@Override
		public boolean isEmpty() {
			return m.isEmpty();
		}

		@Override
		public boolean remove(Object object) {
			return m.remove(object) != null;
		}

		@Override
		public boolean retainAll(Collection<?> collection) {
			return backingSet.retainAll(collection);
		}

		@Override
		public Object[] toArray() {
			return backingSet.toArray();
		}

		@Override
		public <T> T[] toArray(T[] contents) {
			return backingSet.toArray(contents);
		}

		@Override
		public Iterator<E> iterator() {
			return backingSet.iterator();
		}

		@Override
		public int size() {
			return m.size();
		}

		private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
			stream.defaultReadObject();
			backingSet = m.keySet();
		}
	}
}
