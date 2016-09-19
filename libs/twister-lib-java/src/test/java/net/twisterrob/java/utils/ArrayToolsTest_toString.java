package net.twisterrob.java.utils;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayToolsTest_toString {
	@SuppressWarnings("RedundantCast")
	@Test public void testNull() {
		assertEquals(Arrays.toString((boolean[])null), ArrayTools.toString((boolean[])null));
		assertEquals(Arrays.toString((byte[])null), ArrayTools.toString((byte[])null));
		assertEquals(Arrays.toString((short[])null), ArrayTools.toString((short[])null));
		assertEquals(Arrays.toString((char[])null), ArrayTools.toString((char[])null));
		assertEquals(Arrays.toString((int[])null), ArrayTools.toString((int[])null));
		assertEquals(Arrays.toString((long[])null), ArrayTools.toString((long[])null));
		assertEquals(Arrays.toString((float[])null), ArrayTools.toString((float[])null));
		assertEquals(Arrays.toString((double[])null), ArrayTools.toString((double[])null));
		assertEquals(Arrays.toString((String[])null), ArrayTools.toString((String[])null));
		assertEquals(Arrays.toString((Object[])null), ArrayTools.toString((Object[])null));
		assertEquals(Arrays.toString((Object[][][])null), ArrayTools.toString((Object[][][])null));
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(expected = RuntimeException.class)
	public void testNonArray() {
		ArrayTools.toString("hello");
	}

	@Test public void testEmpty() {
		test(0);
	}

	@Test public void testSingle() {
		test(1);
	}

	@Test public void testMany() {
		test(10);
	}

	private void test(int length) {
		assertEquals(Arrays.toString(new boolean[length]), ArrayTools.toString(new boolean[length]));
		assertEquals(Arrays.toString(new byte[length]), ArrayTools.toString(new byte[length]));
		assertEquals(Arrays.toString(new short[length]), ArrayTools.toString(new short[length]));
		assertEquals(Arrays.toString(new char[length]), ArrayTools.toString(new char[length]));
		assertEquals(Arrays.toString(new int[length]), ArrayTools.toString(new int[length]));
		assertEquals(Arrays.toString(new long[length]), ArrayTools.toString(new long[length]));
		assertEquals(Arrays.toString(new float[length]), ArrayTools.toString(new float[length]));
		assertEquals(Arrays.toString(new double[length]), ArrayTools.toString(new double[length]));
		assertEquals(Arrays.toString(new String[length]), ArrayTools.toString(new String[length]));
		assertEquals(Arrays.toString(new Object[length]), ArrayTools.toString(new Object[length]));
	}

	@Test public void testMultidimensional() {
		assertEquals(Arrays.deepToString(new boolean[3][2][1]), ArrayTools.toString(new boolean[3][2][1]));
		assertEquals(Arrays.deepToString(new byte[3][2][1]), ArrayTools.toString(new byte[3][2][1]));
		assertEquals(Arrays.deepToString(new short[3][2][1]), ArrayTools.toString(new short[3][2][1]));
		assertEquals(Arrays.deepToString(new char[3][2][1]), ArrayTools.toString(new char[3][2][1]));
		assertEquals(Arrays.deepToString(new int[3][2][1]), ArrayTools.toString(new int[3][2][1]));
		assertEquals(Arrays.deepToString(new long[3][2][1]), ArrayTools.toString(new long[3][2][1]));
		assertEquals(Arrays.deepToString(new float[3][2][1]), ArrayTools.toString(new float[3][2][1]));
		assertEquals(Arrays.deepToString(new double[3][2][1]), ArrayTools.toString(new double[3][2][1]));
		assertEquals(Arrays.deepToString(new String[3][2][1]), ArrayTools.toString(new String[3][2][1]));
		assertEquals(Arrays.deepToString(new Object[3][2][1]), ArrayTools.toString(new Object[3][2][1]));
	}
}
