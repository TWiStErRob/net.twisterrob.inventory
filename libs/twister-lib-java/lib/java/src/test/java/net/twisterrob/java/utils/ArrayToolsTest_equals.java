package net.twisterrob.java.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayToolsTest_equals {
	@SuppressWarnings("RedundantCast")
	@Test public void testNull() {
		assertTrue(ArrayTools.equals((boolean[])null, (boolean[])null));
		assertTrue(ArrayTools.equals((byte[])null, (byte[])null));
		assertTrue(ArrayTools.equals((short[])null, (short[])null));
		assertTrue(ArrayTools.equals((char[])null, (char[])null));
		assertTrue(ArrayTools.equals((int[])null, (int[])null));
		assertTrue(ArrayTools.equals((long[])null, (long[])null));
		assertTrue(ArrayTools.equals((float[])null, (float[])null));
		assertTrue(ArrayTools.equals((double[])null, (double[])null));
		assertTrue(ArrayTools.equals((String[])null, (String[])null));
		assertTrue(ArrayTools.equals((Object[])null, (Object[])null));
		assertTrue(ArrayTools.equals((Object[][])null, (Object[][])null));
	}

	@SuppressWarnings("RedundantCast")
	@Test public void testNullAsymmetric() {
		assertFalse(ArrayTools.equals(new boolean[0], (boolean[])null));
		assertFalse(ArrayTools.equals(new byte[0], (byte[])null));
		assertFalse(ArrayTools.equals(new short[0], (short[])null));
		assertFalse(ArrayTools.equals(new char[0], (char[])null));
		assertFalse(ArrayTools.equals(new int[0], (int[])null));
		assertFalse(ArrayTools.equals(new long[0], (long[])null));
		assertFalse(ArrayTools.equals(new float[0], (float[])null));
		assertFalse(ArrayTools.equals(new double[0], (double[])null));
		assertFalse(ArrayTools.equals(new String[0], (String[])null));
		assertFalse(ArrayTools.equals(new Object[0], (Object[])null));
		assertFalse(ArrayTools.equals(new Object[0][0], (Object[][])null));
		assertFalse(ArrayTools.equals((boolean[])null, new boolean[0]));
		assertFalse(ArrayTools.equals((byte[])null, new byte[0]));
		assertFalse(ArrayTools.equals((short[])null, new short[0]));
		assertFalse(ArrayTools.equals((char[])null, new char[0]));
		assertFalse(ArrayTools.equals((int[])null, new int[0]));
		assertFalse(ArrayTools.equals((long[])null, new long[0]));
		assertFalse(ArrayTools.equals((float[])null, new float[0]));
		assertFalse(ArrayTools.equals((double[])null, new double[0]));
		assertFalse(ArrayTools.equals((String[])null, new String[0]));
		assertFalse(ArrayTools.equals((Object[])null, new Object[0]));
		assertFalse(ArrayTools.equals((Object[][])null, new Object[0][0]));
	}

	@Test public void testEmpty() {
		assertTrue(ArrayTools.equals(new boolean[0], new boolean[0]));
		assertTrue(ArrayTools.equals(new byte[0], new byte[0]));
		assertTrue(ArrayTools.equals(new short[0], new short[0]));
		assertTrue(ArrayTools.equals(new char[0], new char[0]));
		assertTrue(ArrayTools.equals(new int[0], new int[0]));
		assertTrue(ArrayTools.equals(new long[0], new long[0]));
		assertTrue(ArrayTools.equals(new float[0], new float[0]));
		assertTrue(ArrayTools.equals(new double[0], new double[0]));
		assertTrue(ArrayTools.equals(new String[0], new String[0]));
		assertTrue(ArrayTools.equals(new Object[0], new Object[0]));
	}

	@Test public void testSameValuesSingle() {
		assertTrue(ArrayTools.equals(new boolean[1], new boolean[1]));
		assertTrue(ArrayTools.equals(new byte[1], new byte[1]));
		assertTrue(ArrayTools.equals(new short[1], new short[1]));
		assertTrue(ArrayTools.equals(new char[1], new char[1]));
		assertTrue(ArrayTools.equals(new int[1], new int[1]));
		assertTrue(ArrayTools.equals(new long[1], new long[1]));
		assertTrue(ArrayTools.equals(new float[1], new float[1]));
		assertTrue(ArrayTools.equals(new double[1], new double[1]));
		assertTrue(ArrayTools.equals(new String[] {"1"}, new String[] {"1"}));
		Object value = new Object();
		assertTrue(ArrayTools.equals(new Object[] {value}, new Object[] {value}));
	}

	@Test public void testSameValuesMany() {
		assertTrue(ArrayTools.equals(new boolean[] {true, false, true}, new boolean[] {true, false, true}));
		assertTrue(ArrayTools.equals(new byte[] {1, 2, 3}, new byte[] {1, 2, 3}));
		assertTrue(ArrayTools.equals(new short[] {1, 2, 3}, new short[] {1, 2, 3}));
		assertTrue(ArrayTools.equals(new char[] {1, 2, 3}, new char[] {1, 2, 3}));
		assertTrue(ArrayTools.equals(new int[] {1, 2, 3}, new int[] {1, 2, 3}));
		assertTrue(ArrayTools.equals(new long[] {1, 2, 3}, new long[] {1, 2, 3}));
		assertTrue(ArrayTools.equals(new float[] {1, 2, 3}, new float[] {1, 2, 3}));
		assertTrue(ArrayTools.equals(new double[] {1, 2, 3}, new double[] {1, 2, 3}));
		assertTrue(ArrayTools.equals(new String[] {"1", "2", "3"}, new String[] {"1", "2", "3"}));
		Object value1 = new Object(), value2 = new Object(), value3 = new Object();
		assertTrue(ArrayTools.equals(new Object[] {value1, value2, value3}, new Object[] {value1, value2, value3}));
	}

	@Test public void testDifferentValuesSingle() {
		assertFalse(ArrayTools.equals(new boolean[] {true}, new boolean[] {false}));
		assertFalse(ArrayTools.equals(new byte[] {0}, new byte[] {1}));
		assertFalse(ArrayTools.equals(new short[] {0}, new short[] {1}));
		assertFalse(ArrayTools.equals(new char[] {0}, new char[] {1}));
		assertFalse(ArrayTools.equals(new int[] {0}, new int[] {1}));
		assertFalse(ArrayTools.equals(new long[] {0}, new long[] {1}));
		assertFalse(ArrayTools.equals(new float[] {0}, new float[] {1}));
		assertFalse(ArrayTools.equals(new double[] {0}, new double[] {1}));
		assertFalse(ArrayTools.equals(new String[] {"0"}, new String[] {"1"}));
		assertFalse(ArrayTools.equals(new Object[] {new Object()}, new Object[] {new Object()}));
	}

	@Test public void testDifferentValuesValuesMany() {
		assertFalse(ArrayTools.equals(new boolean[] {true, false, true}, new boolean[] {false, true, false}));
		assertFalse(ArrayTools.equals(new byte[] {1, 2, 3}, new byte[] {4, 5, 6}));
		assertFalse(ArrayTools.equals(new short[] {1, 2, 3}, new short[] {4, 5, 6}));
		assertFalse(ArrayTools.equals(new char[] {1, 2, 3}, new char[] {4, 5, 6}));
		assertFalse(ArrayTools.equals(new int[] {1, 2, 3}, new int[] {4, 5, 6}));
		assertFalse(ArrayTools.equals(new long[] {1, 2, 3}, new long[] {4, 5, 6}));
		assertFalse(ArrayTools.equals(new float[] {1, 2, 3}, new float[] {4, 5, 6}));
		assertFalse(ArrayTools.equals(new double[] {1, 2, 3}, new double[] {4, 5, 6}));
		assertFalse(ArrayTools.equals(new String[] {"1", "2", "3"}, new String[] {"4", "5", "6"}));
		assertFalse(ArrayTools.equals(
				new Object[] {new Object(), new Object(), new Object()},
				new Object[] {new Object(), new Object(), new Object()})
		);
	}

	@Test public void testDifferentTypesAgainstObject() {
		assertFalse(ArrayTools.equals(new Object[1], new boolean[] {false}));
		assertFalse(ArrayTools.equals(new Object[1], new byte[] {1}));
		assertFalse(ArrayTools.equals(new Object[1], new short[] {1}));
		assertFalse(ArrayTools.equals(new Object[1], new char[] {1}));
		assertFalse(ArrayTools.equals(new Object[1], new int[] {1}));
		assertFalse(ArrayTools.equals(new Object[1], new long[] {1}));
		assertFalse(ArrayTools.equals(new Object[1], new float[] {1}));
		assertFalse(ArrayTools.equals(new Object[1], new double[] {1}));
		assertFalse(ArrayTools.equals(new Object[1], new String[] {"1"}));

		assertFalse(ArrayTools.equals(new boolean[] {true}, new Object[1]));
		assertFalse(ArrayTools.equals(new byte[] {0}, new Object[1]));
		assertFalse(ArrayTools.equals(new short[] {0}, new Object[1]));
		assertFalse(ArrayTools.equals(new char[] {0}, new Object[1]));
		assertFalse(ArrayTools.equals(new int[] {0}, new Object[1]));
		assertFalse(ArrayTools.equals(new long[] {0}, new Object[1]));
		assertFalse(ArrayTools.equals(new float[] {0}, new Object[1]));
		assertFalse(ArrayTools.equals(new double[] {0}, new Object[1]));
		assertFalse(ArrayTools.equals(new String[] {"0"}, new Object[1]));
	}

	@Test public void testNonArray() {
		assertTrue(ArrayTools.equals("string", "string"));
		assertFalse(ArrayTools.equals("string1", "string2"));
		assertFalse(ArrayTools.equals(new Object(), new Object()));
	}

	@Test public void testNonArrayAsymmetric() {
		assertFalse(ArrayTools.equals(new Object(), new boolean[] {false}));
		assertFalse(ArrayTools.equals(new Object(), new byte[] {1}));
		assertFalse(ArrayTools.equals(new Object(), new short[] {1}));
		assertFalse(ArrayTools.equals(new Object(), new char[] {1}));
		assertFalse(ArrayTools.equals(new Object(), new int[] {1}));
		assertFalse(ArrayTools.equals(new Object(), new long[] {1}));
		assertFalse(ArrayTools.equals(new Object(), new float[] {1}));
		assertFalse(ArrayTools.equals(new Object(), new double[] {1}));
		assertFalse(ArrayTools.equals(new Object(), new String[] {"1"}));

		assertFalse(ArrayTools.equals(new boolean[] {true}, new Object()));
		assertFalse(ArrayTools.equals(new byte[] {0}, new Object()));
		assertFalse(ArrayTools.equals(new short[] {0}, new Object()));
		assertFalse(ArrayTools.equals(new char[] {0}, new Object()));
		assertFalse(ArrayTools.equals(new int[] {0}, new Object()));
		assertFalse(ArrayTools.equals(new long[] {0}, new Object()));
		assertFalse(ArrayTools.equals(new float[] {0}, new Object()));
		assertFalse(ArrayTools.equals(new double[] {0}, new Object()));
		assertFalse(ArrayTools.equals(new String[] {"0"}, new Object()));
	}
}
