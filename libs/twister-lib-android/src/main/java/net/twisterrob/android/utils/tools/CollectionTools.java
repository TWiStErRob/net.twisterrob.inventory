package net.twisterrob.android.utils.tools;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

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

	/**
	 * Tries to get the last item in the quickes time possible.
	 * @return last element of the set, or <code>null</code> instaed of {@link NoSuchElementException}.
	 */
	public static <T> T last(Set<T> set) {
		return last(set, false);
	}

	/**
	 * Tries to get the last item in the quickes time possible.
	 * @return last element of the set, or <code>null</code> instaed of {@link NoSuchElementException}.
	 */
	public static <T> T last(Set<T> set, boolean hacky) {
		T elem = null;
		try {
			if (set.isEmpty()) {
				// elem is null
			} else if (set instanceof TreeSet) {
				elem = ((TreeSet<T>)set).last();
			} else if (set instanceof ConcurrentSkipListSet) {
				elem = ((ConcurrentSkipListSet<T>)set).last();
			} else if (hacky) {
				if (set.size() < 0) {
					// don't even try to hack, iterator is probably faster
				} else if (set instanceof LinkedHashSet) {
					elem = tryGetLastJava((LinkedHashSet<T>)set);
					if (elem == null) {
						elem = tryGetLastAndroid((LinkedHashSet<T>)set);
					}
				}
			}
			// fall back if others fail
			if (elem == null) {
				for (Iterator<T> iterator = set.iterator(); iterator.hasNext();) {
					elem = iterator.next();
				}
			}
		} catch (NoSuchElementException ex) {
			elem = null;
		}
		return elem;
	}

	@SuppressWarnings("unchecked")
	private static <T> T tryGetLastJava(LinkedHashSet<T> set) {
		T elem = null;
		try {
			Object next = set;
			// let's try: set.map.header.before.key
			Field f;
			// HashSet.(HashMap<T, ?> map).header
			f = next.getClass().getSuperclass().getDeclaredField("map");
			f.setAccessible(true);
			next = f.get(next);
			// LinkedHashMap.(Entry<T, ?> header).before
			f = next.getClass().getDeclaredField("header");
			f.setAccessible(true);
			next = f.get(next);
			// LinkedHashMap.Entry.(LinkedHashMap.Entry<T, ?> extends HashMap.Entry<T, ?> before).key
			f = next.getClass().getDeclaredField("before");
			f.setAccessible(true);
			next = f.get(next);
			// HashMap.Entry.(T key)
			f = next.getClass().getSuperclass().getDeclaredField("key");
			f.setAccessible(true);
			elem = (T)f.get(next);
		} catch (NoSuchFieldException e) {
			// not Sun Java 1.6, give up
		} catch (IllegalArgumentException e) {
			// should not get here
		} catch (IllegalAccessException e) {
			// should not get here
		}
		return elem;
	}

	@SuppressWarnings("unchecked")
	private static <T> T tryGetLastAndroid(LinkedHashSet<T> set) {
		T elem = null;
		try { // android
			Object next = set;
			// let's try: set.backingMap.header.prv.key
			Field f;

			// HashSet.(LinkedHashMap<T, ?> backingMap).header
			f = next.getClass().getSuperclass().getDeclaredField("backingMap");
			f.setAccessible(true);
			next = f.get(next);
			// LinkedHashMap.(LinkedHashMap.LinkedEntry<T, ?> header).prv
			f = next.getClass().getDeclaredField("header");
			f.setAccessible(true);
			next = f.get(next);
			// LinkedHashMap.LinkedEntry.(LinkedHashMap.LinkedEntry<T, ?> extends HashMap.HashMapEntry<T, ?> prv).key
			f = next.getClass().getDeclaredField("prv");
			f.setAccessible(true);
			next = f.get(next);
			// HashMap.Entry.(T key)
			f = next.getClass().getSuperclass().getDeclaredField("key");
			f.setAccessible(true);
			elem = (T)f.get(next);
		} catch (NoSuchFieldException e1) {
			// not Android, give up
		} catch (IllegalArgumentException e) {
			// should not get here
		} catch (IllegalAccessException e) {
			// should not get here
		}
		return elem;
	}
}
