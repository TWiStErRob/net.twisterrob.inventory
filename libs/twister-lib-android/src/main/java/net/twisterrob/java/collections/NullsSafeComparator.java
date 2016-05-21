package net.twisterrob.java.collections;

import java.util.Comparator;

public class NullsSafeComparator<T extends Comparable<? super T>> implements Comparator<T> {
	private final boolean nullsFirst;
	public NullsSafeComparator() {
		this(false);
	}
	public NullsSafeComparator(boolean nullsFirst) {
		this.nullsFirst = nullsFirst;
	}
	@Override public int compare(T lhs, T rhs) {
		if (lhs == null) {
			if (rhs == null) {
				return 0;
			} else {
				return nullsFirst? 1 : -1;
			}
		} else {
			if (rhs == null) {
				return nullsFirst? -1 : 1;
			} else {
				return lhs.compareTo(rhs);
			}
		}
	}
}
