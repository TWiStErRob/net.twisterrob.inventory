package net.twisterrob.android.utils.tostring.stringers.map;

import java.util.Map;

public interface DeepCollection extends Iterable<Map.Entry<?, ?>> {
	String getType();
	int size();
}
