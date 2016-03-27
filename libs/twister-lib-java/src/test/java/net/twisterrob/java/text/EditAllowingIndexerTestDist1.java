package net.twisterrob.java.text;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EditAllowingIndexerTestDist1 extends BaseIndexerTest {
	@Before public void setUp() throws Exception {
		index = new EditAllowingIndexer<>(1);
	}
	@Parameters(name = "{0}") public static Object[][] data() {
		return merge(new String[] {},
				BaseIndexerTest.tests(),
				EditAllowingIndexerTestDist1.tests());
	}

	@SuppressWarnings("SpellCheckingInspection")
	public static Object[][] tests() {
		return new Object[][] {
				test("one character changed", "abc",
						new String[] {
								"abc", NORMAL_MATCH,
								"abd", "last char changed",
								"bbc", "first char changed",
								"adc", "middle char changed",
						},
						new String[] {
								"ghi", NO_MATCH,
						}
				),
				test("one character deleted", "abcd",
						new String[] {
								"abcd", NORMAL_MATCH,
								"bcd", NORMAL_MATCH,
								"acd", NORMAL_MATCH,
								"abd", NORMAL_MATCH,
								"abc", NORMAL_MATCH,
						},
						new String[] {
								"a", NO_MATCH,
								"b", NO_MATCH,
								"c", NO_MATCH,
								"d", NO_MATCH,
								"ac", NO_MATCH,
								"ad", NO_MATCH, // don't match two deletions
						}
				),
				test("one character inserted", "abc",
						new String[] {
								"abc", NORMAL_MATCH,
								"abcd", NORMAL_MATCH,
								"dabc", NORMAL_MATCH,
								"adbc", NORMAL_MATCH,
								"abdc", NORMAL_MATCH,
						},
						new String[] {/* everything matches */}
				),
				test("one neighbor swapped positive", "abcd",
						new String[] {
								"abcd", NORMAL_MATCH,
								// swap one letter
								"bacd", NORMAL_MATCH,
								"acbd", NORMAL_MATCH,
								"abdc", NORMAL_MATCH,
						},
						new String[] {/* everything matches */}
				),
				test("one neighbor swapped negative", "abcd",
						new String[] {/* nothing matches */},
						new String[] {
								// swap too far
								"cbad", NO_MATCH,
								"dbca", NO_MATCH,
						}
				),
				test("one diff misc", "abcd",
						new String[] {
								"abcde", NORMAL_MATCH,
								"acd", NORMAL_MATCH,
								"bcd", NORMAL_MATCH,
								"abcd", NORMAL_MATCH,
						},
						new String[] {/* everything matches */}
				),
		};
	}
}
