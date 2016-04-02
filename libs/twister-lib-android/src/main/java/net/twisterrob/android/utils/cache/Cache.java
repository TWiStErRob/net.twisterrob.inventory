package net.twisterrob.android.utils.cache;

/** @deprecated use Glide */
@Deprecated @SuppressWarnings({"deprecation", "RedundantThrows"})
public interface Cache<K, V> {
	V get(K key) throws Exception;

	void put(K key, V value) throws Exception;
}
