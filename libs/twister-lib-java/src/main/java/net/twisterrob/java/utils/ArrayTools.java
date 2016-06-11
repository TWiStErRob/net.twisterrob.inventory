package net.twisterrob.java.utils;

import java.util.Arrays;

public final class ArrayTools {
	private ArrayTools() {
		// prevent instantiation
	}
	public static <T> int safeLength(T[] arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(int[] arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(long[] arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(double[] arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(short[] arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(char[] arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(float[] arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(boolean[] arr) {
		return arr == null? 0 : arr.length;
	}
	public static int safeLength(byte[] arr) {
		return arr == null? 0 : arr.length;
	}
	public static String toString(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof Object[]) { // T[]
			return Arrays.toString((Object[])value);
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
}
