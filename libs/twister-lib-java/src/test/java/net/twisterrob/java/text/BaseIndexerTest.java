package net.twisterrob.java.text;

import java.util.*;

import org.hamcrest.*;
import org.junit.*;
import org.junit.runners.Parameterized.Parameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import net.twisterrob.java.text.Indexer.MatchResult;

@SuppressWarnings({"WeakerAccess", "JUnit3StyleTestMethodInJUnit4Class"})
public abstract class BaseIndexerTest {
	protected static final String NO_MATCH = "no match";
	protected static final String NORMAL_MATCH = "normal match";

	protected Indexer<String> index;

	@Parameter @SuppressWarnings("unused") public String name;
	@Parameter(value = 1) public String toMatch;
	@Parameter(value = 2) public String[] positive;
	@Parameter(value = 3) public String[] negative;

	@SuppressWarnings("SpellCheckingInspection")
	public static Object[][] tests() {
		return new Object[][] {
				test("empty", "anything",
						new String[] {/* nothing matches */},
						new String[] {/* everything matches */}
				),
				test("no match", "jkl",
						new String[] {/* nothing matches */},
						new String[] {
								"abc", NO_MATCH,
								"def", NO_MATCH,
								"ghi", NO_MATCH
						}
				),
				test("single word", "word",
						new String[] {
								"word", NORMAL_MATCH
						},
						new String[] {/* everything matches */}
				),
				test("multiple distinct", "jkl",
						new String[] {
								"jkl", NORMAL_MATCH,
						},
						new String[] {
								"abc", NO_MATCH,
								"def", NO_MATCH,
								"ghi", NO_MATCH,
								"mno", NO_MATCH
						}
				),
				test("duplicate", "abc",
						new String[] {
								"abc", NORMAL_MATCH,
								"abc", NORMAL_MATCH + " (different source)"
						},
						new String[] {
								"def", NO_MATCH
						}
				),
		};
	}
	protected static Object[] test(String name, String toMatch, String[] positive, String[] negative) {
		return new Object[] {name, toMatch, positive, negative};
	}

	@Before public void check() {
		final int even = 0;
		assertEquals(even, positive.length % 2);
		assertEquals(even, negative.length % 2);
	}

	/** Positive words match when checked together with negative ones. */
	@Test public void testCombined() {
		index(positive);
		index(negative);
		checkMatches(positive);
	}

	/** Positive words match when checked alone. */
	@Test public void testMatches() {
		index(positive);
		checkMatches(positive);
	}

	/** Negative words don't match when checked alone. */
	@Test public void testNoMatch() {
		index(negative);
		checkMatches();
	}

	private void index(String... input) {
		for (int i = 0; i < input.length; i += 2) {
			index.add(input[i], input[i + 1]);
		}
	}
	private void checkMatches(String... input) {
		Collection<Indexer.MatchResult<String>> suggestions = index.match(toMatch);
		@SuppressWarnings({"rawtypes", "unchecked"}) Matcher<MatchResult<String>>[] matchers =
				new Matcher[input.length / 2];
		for (int i = 0; i < input.length; i += 2) {
			matchers[i / 2] = new MatchForReason<>(input[i], input[i + 1]);
		}
		//noinspection unchecked
		assertThat(suggestions, containsInAnyOrder(matchers));
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	protected static Object[][] merge(String[] excludes, Object[][]... datas) {
		Collection<String> excluded = new HashSet<>(Arrays.asList(excludes));
		int len = 0;
		for (Object[][] data : datas) {
			for (Object[] test : data) {
				if (!excluded.contains(test[0])) {
					len++;
				}
			}
		}
		Object[][] merged = new Object[len][];
		int pos = 0;
		for (Object[][] data : datas) {
			for (Object[] test : data) {
				if (!excluded.contains(test[0])) {
					merged[pos++] = test;
				}
			}
		}
		return merged;
	}

	protected static class MatchForReason<T> extends TypeSafeMatcher<Indexer.MatchResult<T>> {
		private final CharSequence match;
		private final T source;

		public MatchForReason(CharSequence match, T source) {
			this.match = match;
			this.source = source;
		}

		@Override protected boolean matchesSafely(Indexer.MatchResult<T> item) {
			return item.source == source;
		}

		@Override public void describeTo(Description description) {
			description.appendText("match(").appendValue(match).appendText("~").appendValue(source).appendText(")");
		}
	}
}
