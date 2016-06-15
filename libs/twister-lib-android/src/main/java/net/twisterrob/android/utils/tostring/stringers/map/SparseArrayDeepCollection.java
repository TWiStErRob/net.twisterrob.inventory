package net.twisterrob.android.utils.tostring.stringers.map;

import java.util.Iterator;
import java.util.Map.Entry;

import android.util.SparseArray;

import net.twisterrob.android.utils.tools.AndroidTools;

public class SparseArrayDeepCollection implements DeepCollection {
	private final SparseArray<?> sparseArray;

	public SparseArrayDeepCollection(SparseArray<?> sparseArray) {
		this.sparseArray = sparseArray;
	}
	@Override public String getType() {
		return AndroidTools.debugType(sparseArray);
	}
	@Override public int size() {
		return sparseArray.size();
	}
	@Override public Iterator<Entry<?, ?>> iterator() {
		return new Iterator<Entry<?, ?>>() {
			private int index;
			@Override public boolean hasNext() {
				return index < sparseArray.size();
			}
			@Override public Entry<?, ?> next() {
				index++;
				return entry;
			}
			@Override public void remove() {
				throw new UnsupportedOperationException();
			}

			private final Entry<?, ?> entry = new Entry<Integer, Object>() {
				@Override public Integer getKey() {
					return sparseArray.keyAt(index);
				}
				@Override public Object getValue() {
					return sparseArray.valueAt(index);
				}
				@Override public Object setValue(Object object) {
					throw new UnsupportedOperationException();
				}
			};
		};
	}
}
