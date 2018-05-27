package net.twisterrob.java.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayToolsTest_safeLength$reflective {
	@Test public void testNull() {
		assertEquals(0, ArrayTools.safeLength((Object)null));
	}

	@Test(expected = RuntimeException.class)
	public void testNonArray() {
		ArrayTools.safeLength("hello");
	}

	@Test public void testEmpty() {
		assertEquals(0, ArrayTools.safeLength((Object)new boolean[0]));
		assertEquals(0, ArrayTools.safeLength((Object)new byte[0]));
		assertEquals(0, ArrayTools.safeLength((Object)new short[0]));
		assertEquals(0, ArrayTools.safeLength((Object)new char[0]));
		assertEquals(0, ArrayTools.safeLength((Object)new int[0]));
		assertEquals(0, ArrayTools.safeLength((Object)new long[0]));
		assertEquals(0, ArrayTools.safeLength((Object)new float[0]));
		assertEquals(0, ArrayTools.safeLength((Object)new double[0]));
		assertEquals(0, ArrayTools.safeLength((Object)new String[0]));
		assertEquals(0, ArrayTools.safeLength((Object)new Object[0]));
	}

	@Test public void testSome() {
		assertEquals(2, ArrayTools.safeLength((Object)new boolean[2]));
		assertEquals(2, ArrayTools.safeLength((Object)new byte[2]));
		assertEquals(2, ArrayTools.safeLength((Object)new short[2]));
		assertEquals(2, ArrayTools.safeLength((Object)new char[2]));
		assertEquals(2, ArrayTools.safeLength((Object)new int[2]));
		assertEquals(2, ArrayTools.safeLength((Object)new long[2]));
		assertEquals(2, ArrayTools.safeLength((Object)new float[2]));
		assertEquals(2, ArrayTools.safeLength((Object)new double[2]));
		assertEquals(2, ArrayTools.safeLength((Object)new String[2]));
		assertEquals(2, ArrayTools.safeLength((Object)new Object[2]));
	}

	@Test public void testMultidimensional() {
		assertEquals(3, ArrayTools.safeLength((Object)new boolean[3][2]));
		assertEquals(3, ArrayTools.safeLength((Object)new byte[3][2]));
		assertEquals(3, ArrayTools.safeLength((Object)new short[3][2]));
		assertEquals(3, ArrayTools.safeLength((Object)new char[3][2]));
		assertEquals(3, ArrayTools.safeLength((Object)new int[3][2]));
		assertEquals(3, ArrayTools.safeLength((Object)new long[3][2]));
		assertEquals(3, ArrayTools.safeLength((Object)new float[3][2]));
		assertEquals(3, ArrayTools.safeLength((Object)new double[3][2]));
		assertEquals(3, ArrayTools.safeLength((Object)new String[3][2]));
		assertEquals(3, ArrayTools.safeLength((Object)new Object[3][2]));
	}
}
