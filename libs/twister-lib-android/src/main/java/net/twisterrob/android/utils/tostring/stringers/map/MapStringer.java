package net.twisterrob.android.utils.tostring.stringers.map;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.*;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;

import net.twisterrob.android.utils.tools.AndroidTools;

public class MapStringer {
	private static final Logger LOG = LoggerFactory.getLogger(MapStringer.class);

	public static final MapStringer SHORT = new MapStringer.Builder()
			.aroundType("(", ")")
			.aroundList("#{", "}")
			.aroundListItem("", ", ")
			.build();
	public static final MapStringer LONG = new MapStringer.Builder()
			.aroundType("", " of ")
			.aroundList("\n", "")
			.aroundListItem("\t", "\n")
			.build();

	private final String preType;
	private final String postType;
	private final String start;
	private final String preItem;
	private final String postItem;
	private final String end;
	private final String mapping;
	private final String singleItem;
	private int level;
	private StringBuilder sb;
	private final Context context;
	private final KeyFormatter formatter;

	protected MapStringer(String preType, String postType, String start, String preItem, String postItem, String end,
			String mapping, String singleItem, int level) {
		this.preType = preType;
		this.postType = postType;
		this.start = start;
		this.preItem = preItem;
		this.postItem = postItem;
		this.end = end;
		this.mapping = mapping;
		this.singleItem = singleItem;
		setLevel(level);
		this.context = AndroidTools.getContext();
		formatter = new ViewIDKeyFormatter(context);
	}
	public MapStringer start() {
		MapStringer build = new Builder(this).startingIndent(0).build();
		build.resetBuilder();
		return build;
	}
	protected void indent() {
		setLevel(level + 1);
	}
	protected void unindent() {
		setLevel(level - 1);
	}
	protected void setLevel(int level) {
		assertValidIndent(level);
		this.level = level;
	}
	public void resetBuilder() {
		sb = new StringBuilder();
	}

	public String finish() {
		return sb.toString();
	}

	public void toStringRec(DeepCollection coll, boolean autoindent) {
		if (autoindent) {
			indent();
		}
		try {
			int size = coll.size();
			sb.append(preType).append(coll.getType()).append(postType).append(size);
			if (size == 0) {
				return;
			}
			boolean shortcut = size <= 1;
			if (shortcut) {
				sb.append(singleItem);
			} else {
				sb.append(start);
			}

			for (Iterator<Map.Entry<?, ?>> it = coll.iterator(); it.hasNext(); ) {
				if (!shortcut) {
					for (int i = 0; i < level; i++) {
						sb.append(preItem);
					}
				}
				Map.Entry<?, ?> entry = it.next();
				formatKey(entry);
				sb.append(mapping);

				Object value = entry.getValue();
				if (value instanceof Bundle) {
					Bundle val = (Bundle)value;
					toStringRec(new BundleDeepCollection(val), !shortcut);
				} else if (value instanceof SparseArray) {
					SparseArray<?> arr = (SparseArray<?>)value;
					toStringRec(new SparseArrayDeepCollection(arr), !shortcut);
				} else {
					formatValue(entry);
				}
				if (it.hasNext()) {
					sb.append(postItem);
				}
			}
			if (!shortcut) {
				sb.append(end);
			}
		} finally {
			if (autoindent) {
				unindent();
			}
		}
	}
	private void formatKey(Entry<?, ?> entry) {
		Object key = entry.getKey();
		if (key instanceof Integer && formatter.needResolveIDs(key, null)) {
			sb.append(AndroidTools.toNameString(context, (Integer)key));
		} else {
			sb.append(key);
		}
	}
	private void formatValue(Entry<?, ?> entry) {
		Object value = entry.getValue();
		if (value instanceof Integer && formatter.needResolveIDs(entry.getKey(), value)) {
			sb.append(AndroidTools.toNameString(context, (Integer)value));
		} else {
			sb.append(AndroidTools.toString(value));
		}
	}

	public static class Builder {
		private String preType = "";
		private String postType = "";
		private String start = "";
		private String preItem = "";
		private String postItem = "";
		private String end = "";
		private String mapping = " -> ";
		private String singleItem = ": ";
		private int level = 0;
		public Builder() {
		}
		public Builder(MapStringer other) {
			this.preType = other.preType;
			this.postType = other.postType;
			this.start = other.start;
			this.preItem = other.preItem;
			this.postItem = other.postItem;
			this.end = other.end;
			this.mapping = other.mapping;
			this.singleItem = other.singleItem;
			this.level = other.level;
		}

		public Builder startingIndent(int level) {
			assertValidIndent(level);
			this.level = level;
			return this;
		}

		public Builder aroundType(String preType, String postType) {
			this.preType = preType;
			this.postType = postType;
			return this;
		}
		public Builder aroundList(String start, String end) {
			this.start = start;
			this.end = end;
			return this;
		}
		public Builder aroundListItem(String preItem, String postItem) {
			this.preItem = preItem;
			this.postItem = postItem;
			return this;
		}

		public MapStringer build() {
			return new MapStringer(preType, postType, start, preItem, postItem, end, mapping, singleItem, level);
		}
	}
	private static void assertValidIndent(int level) {
		if (level < 0) {
			throw new IllegalStateException("Cannot use negative indent value.");
		}
	}
}
