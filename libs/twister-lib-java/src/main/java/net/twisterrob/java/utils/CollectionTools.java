package net.twisterrob.java.utils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.annotation.Nonnull;

public final class CollectionTools {
	private CollectionTools() {
		// prevent instantiation
	}

	public static void ensureIndexValid(final List<?> list, int i) {
		i -= list.size(); // don't need to add existing indices
		while (i-- >= 0) { // add remaining
			list.add(null);
		}
	}

	@SafeVarargs
	public static <T> T coalesce(final T... objects) {
		for (T t : objects) {
			if (t != null) {
				return t;
			}
		}
		return null;
	}

	public static <T> List<T> remove(final List<T> list, final Class<? extends T> clazz) {
		List<T> removed = new LinkedList<>();
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
			return new SetFromMap<>(map);
		}
		throw new IllegalArgumentException();
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <T> T[] nonNull(T... listOrNull) {
		return listOrNull != null? listOrNull : (T[])new Object[0];
	}

	public static <T> List<T> nonNull(List<T> listOrNull) {
		return listOrNull != null? listOrNull : Collections.<T>emptyList();
	}

	public static <T> Collection<T> nonNull(Collection<T> listOrNull) {
		return listOrNull != null? listOrNull : Collections.<T>emptyList();
	}

	public static <K, V> Map<K, V> nonNull(Map<K, V> listOrNull) {
		return listOrNull != null? listOrNull : Collections.<K, V>emptyMap();
	}

	public static <T> Set<T> nonNull(Set<T> listOrNull) {
		return listOrNull != null? listOrNull : Collections.<T>emptySet();
	}

	private static class SetFromMap<E> extends AbstractSet<E> implements Serializable {
		private static final long serialVersionUID = 2454657854757543876L;

		// must named as it, to pass serialization compatibility test.
		private final Map<E, Boolean> m;
		private transient Set<E> backingSet;

		SetFromMap(final Map<E, Boolean> map) {
			m = map;
			backingSet = map.keySet();
		}
		@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
		@Override public boolean equals(Object object) {
			return backingSet.equals(object);
		}
		@Override public int hashCode() {
			return backingSet.hashCode();
		}
		@Override public boolean add(E object) {
			return m.put(object, Boolean.TRUE) == null;
		}
		@Override public void clear() {
			m.clear();
		}
		@Override public String toString() {
			return backingSet.toString();
		}
		@Override public boolean contains(Object object) {
			return backingSet.contains(object);
		}
		@Override public boolean containsAll(@Nonnull Collection<?> collection) {
			return backingSet.containsAll(collection);
		}
		@Override public boolean isEmpty() {
			return m.isEmpty();
		}
		@Override public boolean remove(Object object) {
			return m.remove(object) != null;
		}
		@Override public boolean retainAll(@Nonnull Collection<?> collection) {
			return backingSet.retainAll(collection);
		}
		@Override public @Nonnull Object[] toArray() {
			return backingSet.toArray();
		}
		@Override public @Nonnull <T> T[] toArray(@Nonnull T[] contents) {
			//noinspection SuspiciousToArrayCall TODEL https://youtrack.jetbrains.com/issue/IDEA-154052
			return backingSet.toArray(contents);
		}
		@Override public @Nonnull Iterator<E> iterator() {
			return backingSet.iterator();
		}
		@Override public int size() {
			return m.size();
		}
		private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
			stream.defaultReadObject();
			backingSet = m.keySet();
		}
	}

	/**
	 * Tries to get the last item in the quickest time complexity possible.
	 * @return last element of the set, or <code>null</code> instead of {@link NoSuchElementException}.
	 */
	public static <T> T last(Set<T> set) {
		return last(set, false);
	}

	/**
	 * Tries to get the last item in the quickest time complexity possible.
	 * @return last element of the set, or <code>null</code> instead of {@link NoSuchElementException}.
	 */
	public static <T> T last(Set<T> set, boolean hacky) {
		T lastItem = null;
		try {
			if (set.isEmpty()) {
				// lastItem is null
			} else if (set instanceof TreeSet) {
				lastItem = ((TreeSet<T>)set).last();
			} else if (set instanceof ConcurrentSkipListSet) {
				lastItem = ((ConcurrentSkipListSet<T>)set).last();
			} else if (hacky) {
				if (set.size() < 0) {
					// don't even try to hack, iterator is probably faster
				} else if (set instanceof LinkedHashSet) {
					lastItem = tryGetLastJava((LinkedHashSet<T>)set);
					if (lastItem == null) {
						lastItem = tryGetLastAndroid((LinkedHashSet<T>)set);
					}
				}
			}
			// fall back if others fail
			if (lastItem == null) {
				for (T item : set) {
					lastItem = item;
				}
			}
		} catch (NoSuchElementException ex) {
			lastItem = null;
		}
		return lastItem;
	}

	@SuppressWarnings("unchecked")
	private static <T> T tryGetLastJava(LinkedHashSet<T> set) {
		T elem = null;
		//noinspection TryWithIdenticalCatches TODEL see https://youtrack.jetbrains.com/issue/IDEA-154035 and IDEA-154037
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
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			// should not get here
		}
		return elem;
	}

	@SuppressWarnings("unchecked")
	private static <T> T tryGetLastAndroid(LinkedHashSet<T> set) {
		T elem = null;
		//noinspection TryWithIdenticalCatches TODEL see https://youtrack.jetbrains.com/issue/IDEA-154035 and IDEA-154037
		try {
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
		} catch (NoSuchFieldException ex) {
			// not Android, give up
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			// should not get here
		}
		return elem;
	}

	/**
	 * Reverses a map so that keys will be indexed by values. <code>null</code> values are skipped.
	 * @param from source map
	 * @param to target map
	 * @return target map
	 * @throws IllegalArgumentException if duplicate values are found in the source map
	 */
	public static <K, V> Map<V, K> reverseMap(Map<? extends K, ? extends V> from, Map<V, K> to) {
		for (Entry<? extends K, ? extends V> in : from.entrySet()) {
			if (in.getValue() == null) {
				continue;
			}
			if (null != to.put(in.getValue(), in.getKey())) {
				throw new IllegalArgumentException("The values are not unique in the source map!");
			}
		}
		return to;
	}
}
