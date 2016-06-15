package net.twisterrob.android.utils.tostring.stringers.map;

import java.util.*;

import net.twisterrob.android.utils.tools.AndroidTools;

public class MapDeepCollection implements DeepCollection {
	private final Map<?, ?> map;
	public MapDeepCollection(Map<?, ?> map) {
		this.map = map;
	}

	@Override public String getType() {
		return AndroidTools.debugType(map);
	}
	@Override public int size() {
		return map.size();
	}
	@Override public Iterator<Map.Entry<?, ?>> iterator() {
		@SuppressWarnings("unchecked") // entrySet returns Iterable<? extends Map.Entry<?, ?>> for some reason
				Iterable<Map.Entry<?, ?>> entries = (Iterable<Map.Entry<?, ?>>)(Iterable<?>)map.entrySet();
		return entries.iterator();
	}
}
