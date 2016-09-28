package net.twisterrob.java.utils;

import java.util.Arrays;

public final class ArrayTools {
	private ArrayTools() {
		throw new IllegalAccessError("This static utility class cannot be instantiated");
	}

	@SafeVarargs
	public static <T> int safeLength(T... arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(int... arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(long... arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(double... arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(short... arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(char... arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(float... arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(boolean... arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(byte... arr) {
		return arr == null? 0 : arr.length;
	}

	public static int safeLength(Object value) {
		if (value == null) {
			return 0;
		}
		if (value instanceof Object[]) { // T[], primitive[]...[]
			return safeLength((Object[])value);
		} else if (value instanceof int[]) {
			return safeLength((int[])value);
		} else if (value instanceof float[]) {
			return safeLength((float[])value);
		} else if (value instanceof char[]) {
			return safeLength((char[])value);
		} else if (value instanceof double[]) {
			return safeLength((double[])value);
		} else if (value instanceof boolean[]) {
			return safeLength((boolean[])value);
		} else if (value instanceof long[]) {
			return safeLength((long[])value);
		} else if (value instanceof short[]) {
			return safeLength((short[])value);
		} else if (value instanceof byte[]) {
			return safeLength((byte[])value);
		} else {
			throw new IllegalArgumentException("Object " + value + " must be of an array type.");
		}
	}

	/**
	 * To be used when type is unknown. If the type is known, use the overloaded methods in {@link java.util.Arrays}!
	 * @see java.util.Arrays#equals
	 * @see java.util.Arrays#deepEquals
	 */
	@SuppressWarnings("SimplifiableIfStatement")
	public static boolean equals(Object arr1, Object arr2) {
		if (arr1 == null || arr2 == null) {
			return arr1 == arr2;
		}
		if (arr1 instanceof Object[] && arr2 instanceof Object[]) { // T[], primitive[]...[]
			return Arrays.deepEquals((Object[])arr1, (Object[])arr2);
		} else if (arr1 instanceof int[] && arr2 instanceof int[]) {
			return Arrays.equals((int[])arr1, (int[])arr2);
		} else if (arr1 instanceof float[] && arr2 instanceof float[]) {
			return Arrays.equals((float[])arr1, (float[])arr2);
		} else if (arr1 instanceof char[] && arr2 instanceof char[]) {
			return Arrays.equals((char[])arr1, (char[])arr2);
		} else if (arr1 instanceof double[] && arr2 instanceof double[]) {
			return Arrays.equals((double[])arr1, (double[])arr2);
		} else if (arr1 instanceof boolean[] && arr2 instanceof boolean[]) {
			return Arrays.equals((boolean[])arr1, (boolean[])arr2);
		} else if (arr1 instanceof long[] && arr2 instanceof long[]) {
			return Arrays.equals((long[])arr1, (long[])arr2);
		} else if (arr1 instanceof short[] && arr2 instanceof short[]) {
			return Arrays.equals((short[])arr1, (short[])arr2);
		} else if (arr1 instanceof byte[] && arr2 instanceof byte[]) {
			return Arrays.equals((byte[])arr1, (byte[])arr2);
		} else {
			// Objects arr1 and arr2 must be both arrays of the same type.
			// Let's fall back to normal equals, so there's no confusing exceptions.
			return arr1.equals(arr2);
		}
	}

	/**
	 * To be used when type is unknown. If the type is known, use the overloaded methods in {@link java.util.Arrays}!
	 * @see java.util.Arrays#toString
	 * @see java.util.Arrays#deepToString
	 */
	public static String toString(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof Object[]) { // T[], primitive[]...[]
			return Arrays.deepToString((Object[])value);
		} else if (value instanceof int[]) {
			return Arrays.toString((int[])value);
		} else if (value instanceof float[]) {
			return Arrays.toString((float[])value);
		} else if (value instanceof char[]) {
			return Arrays.toString((char[])value);
		} else if (value instanceof double[]) {
			return Arrays.toString((double[])value);
		} else if (value instanceof boolean[]) {
			return Arrays.toString((boolean[])value);
		} else if (value instanceof long[]) {
			return Arrays.toString((long[])value);
		} else if (value instanceof short[]) {
			return Arrays.toString((short[])value);
		} else if (value instanceof byte[]) {
			return Arrays.toString((byte[])value);
		} else {
			throw new IllegalArgumentException("Object " + value + " must be of an array type.");
		}
	}

	/**
	 * @see <a href="http://stackoverflow.com/a/784842/253468">How can I concatenate two arrays in Java?</a>
	 */
	@SafeVarargs
	public static <T> T[] concat(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
}
