package net.twisterrob.java.collections;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/** @see <a href="http://stackoverflow.com/a/25634084/253468">SO</a> */
public class WeakValueHashMap<K, V> extends HashMap<K, WeakReference<V>> {
	public V getValue(K key) {
		WeakReference<V> weakRef = super.get(key);
		if (weakRef == null) {
			return null;
		}
		V result = weakRef.get();
		if (result == null) {
			// edge case where the key exists but the object has been garbage collected
			// we remove the key from the table, because tables are slower the more
			// keys they have (@kisp's comment)
			remove(key);
		}
		return result;
	}
	public void putValue(K key, V value) {
		put(key, new WeakReference<>(value));
	}
}
