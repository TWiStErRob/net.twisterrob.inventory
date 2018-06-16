package net.twisterrob.java.text;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EditAllowingIndexerTestDist2 extends BaseIndexerTest {
	@Before public void setUp() {
		index = new EditAllowingIndexer<>(2);
	}

	@Parameters(name = "{0}") public static Object[][] data() {
		return merge(new String[] {
						"one neighbor swapped negative", // far swap is considered two changes, so they match
						"one character deleted", // negative tests contain two deletions, which match with this
				},
				BaseIndexerTest.tests(),
				EditAllowingIndexerTestDist1.tests(),
				EditAllowingIndexerTestDist2.tests());
	}

	@SuppressWarnings("SpellCheckingInspection")
	public static Object[][] tests() {
		return new Object[][] {
				test("two chararacters changed", "abc",
						new String[] {
								"abc", "1",
								"abd", "2",
								"bbc", "3",
								"adc", "3",
								"aef", "4",
								"dbf", "5",
								"dec", "5",
						},
						new String[] {
								"def", "4",
						}
				),
				test("one far swap", "abcd",
						new String[] {
								"cbad", "is two changes",
								"dbca", "is two changes",
						},
						new String[] {
						}
				),
				test("two characters deleted", "abcdef",
						new String[] {
								"abcdef", "0",
								"bcdef", "1",
								"acdef", "1",
								"abdef", "1",
								"abcef", "1",
								"abcdf", "1",
								"abcde", "1",
								"cdef", "2",
								"adef", "2",
								"abcf", "2",
								"abcd", "2",
								"bcde", "3",
								"bcdf", "3",
								"bdef", "3",
								"acde", "3",
								"abce", "3",
						},
						new String[] {
								"abc", "4",
								"def", "4",
						}
				),
				test("two characters inserted", "abc",
						new String[] {
								"abc", "0",
								"abcd", "1",
								"dabc", "1",
								"adbc", "1",
								"abdc", "1",
								"abcde", "2",
								"deabc", "2",
								"adebc", "2",
								"abdec", "2",
								"adbec", "3",
								"dabec", "3",
								"dabce", "3",
								"abdce", "3",
						},
						new String[] {
								"abcdef", "4",
								"defabc", "4",
								"daebfc", "4",
								"daebfcg", "4",
						}
				),
				test("two misc", "abcde",
						new String[] {
								"abcde", "0",
								"abcdef", "1",
								"gabcdef", "1",
								"abcd", "2",
								"abce", "2",
								"bcde", "2",
								"acdef", "3",
								"fgcde", "3",
								"bcdef", "3",
						},
						new String[] {
								"adefg", "4",
						}
				),
		};
	}
}
