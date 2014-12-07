package net.twisterrob.inventory.android.utils;

import java.util.*;

public class Plurals {
	public static final int MAX_PLURAL_LENGTH = 4;

	/** @return <code>[ list.size(), fixed[...], list[0..3] ]</code> */
	public static Object[] toPluralArgs(Collection<?> list, Object... fixed) {
		Object[] result = new Object[1 + Math.min(MAX_PLURAL_LENGTH, list.size())];
		result[0] = list.size();
		Iterator<?> it = list.iterator();
		for (int i = 1; i < result.length; i++) {
			assert it.hasNext() :
					"Collection size=" + result[0] + ", but it.next() can be called only " + (i - 1) + " times";
			result[i] = it.next();
		}
		return result;
	}

	/** @return <code>[ arr.length, fixed[...], arr[0..3] ]</code> */
	public static Object[] toPluralArgs(Object[] arr, Object... fixed) {
		int args = Math.min(MAX_PLURAL_LENGTH, arr.length);
		Object[] result = new Object[1 + fixed.length + args];
		result[0] = arr.length;
		System.arraycopy(fixed, 0, result, 1, fixed.length);
		System.arraycopy(arr, 0, result, 1 + fixed.length, args);
		return result;
	}

	/** @return <code>[ arr.length, fixed[...], arr[0..3] ]</code> */
	public static Object[] toPluralArgs(long[] arr, Object... fixed) {
		int args = Math.min(MAX_PLURAL_LENGTH, arr.length);
		Object[] result = new Object[1 + fixed.length + args];
		result[0] = arr.length;
		System.arraycopy(fixed, 0, result, 1, fixed.length);
		for (int start = 1 + fixed.length, i = 0; i < args; ++i) {
			result[start + i] = arr[i];
		}
		return result;
	}
}
