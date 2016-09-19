package net.twisterrob.java.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayToolsTest_safeLength$overloads {
	@Test public void testNull() {
		assertEquals(0, ArrayTools.safeLength((boolean[])null));
		assertEquals(0, ArrayTools.safeLength((byte[])null));
		assertEquals(0, ArrayTools.safeLength((short[])null));
		assertEquals(0, ArrayTools.safeLength((char[])null));
		assertEquals(0, ArrayTools.safeLength((int[])null));
		assertEquals(0, ArrayTools.safeLength((long[])null));
		assertEquals(0, ArrayTools.safeLength((float[])null));
		assertEquals(0, ArrayTools.safeLength((double[])null));
		assertEquals(0, ArrayTools.safeLength((String[])null));
		assertEquals(0, ArrayTools.safeLength((Object[])null));
		assertEquals(0, ArrayTools.safeLength((Object[][][])null));
	}

	@Test public void testEmpty() {
		assertEquals(0, ArrayTools.safeLength(new boolean[0]));
		assertEquals(0, ArrayTools.safeLength(new byte[0]));
		assertEquals(0, ArrayTools.safeLength(new short[0]));
		assertEquals(0, ArrayTools.safeLength(new char[0]));
		assertEquals(0, ArrayTools.safeLength(new int[0]));
		assertEquals(0, ArrayTools.safeLength(new long[0]));
		assertEquals(0, ArrayTools.safeLength(new float[0]));
		assertEquals(0, ArrayTools.safeLength(new double[0]));
		assertEquals(0, ArrayTools.safeLength(new String[0]));
		assertEquals(0, ArrayTools.safeLength(new Object[0]));
	}

	@Test public void testSome() {
		assertEquals(2, ArrayTools.safeLength(new boolean[2]));
		assertEquals(2, ArrayTools.safeLength(new byte[2]));
		assertEquals(2, ArrayTools.safeLength(new short[2]));
		assertEquals(2, ArrayTools.safeLength(new char[2]));
		assertEquals(2, ArrayTools.safeLength(new int[2]));
		assertEquals(2, ArrayTools.safeLength(new long[2]));
		assertEquals(2, ArrayTools.safeLength(new float[2]));
		assertEquals(2, ArrayTools.safeLength(new double[2]));
		assertEquals(2, ArrayTools.safeLength(new String[2]));
		assertEquals(2, ArrayTools.safeLength(new Object[2]));
	}

	@Test public void testMultidimensional() {
		assertEquals(3, ArrayTools.safeLength(new boolean[3][2]));
		assertEquals(3, ArrayTools.safeLength(new byte[3][2]));
		assertEquals(3, ArrayTools.safeLength(new short[3][2]));
		assertEquals(3, ArrayTools.safeLength(new char[3][2]));
		assertEquals(3, ArrayTools.safeLength(new int[3][2]));
		assertEquals(3, ArrayTools.safeLength(new long[3][2]));
		assertEquals(3, ArrayTools.safeLength(new float[3][2]));
		assertEquals(3, ArrayTools.safeLength(new double[3][2]));
		assertEquals(3, ArrayTools.safeLength(new String[3][2]));
		assertEquals(3, ArrayTools.safeLength(new Object[3][2]));
	}
}
