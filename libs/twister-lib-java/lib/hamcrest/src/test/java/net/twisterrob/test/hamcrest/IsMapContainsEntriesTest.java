package net.twisterrob.test.hamcrest;

import java.util.*;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class IsMapContainsEntriesTest {

	@Test
	public void testEmptyMap() {
		Map<Integer, String> input = Collections.emptyMap();

		assertThat(input, anEmptyMap());
		assertThat(input, IsMapContainsEntries.containsEntries(/*nothing*/));
	}

	@Test
	public void testSingleItem() {
		Map<Integer, String> input = Collections.singletonMap(0, "");

		assertThat(input, IsMapContainsEntries.containsEntries(hasEntry(0, "")));
	}

	@Test
	public void testMultiItems() {
		Map<Integer, String> input = new HashMap<>();
		input.put(0, "zero");
		input.put(1, "one");
		input.put(2, "two");

		assertThat(input, IsMapContainsEntries.containsEntries(
				hasEntry(0, "zero"),
				hasEntry(1, "one"),
				hasEntry(2, "two")
		));
	}

	@Test
	public void testMultipleMatchingForKey() {
		Map<Integer, String> input = new HashMap<>();
		input.put(0, "zero");
		input.put(1, "one");
		input.put(2, "two");

		assertThat(input, IsMapContainsEntries.containsEntries(
				hasEntry(lessThan(2), is("zero")),
				hasEntry(lessThan(2), is("one")),
				hasEntry(2, "two")
		));
	}

	@Test
	public void testMultipleMatchingForValue() {
		Map<Integer, String> input = new HashMap<>();
		input.put(0, "zero");
		input.put(1, "one");
		input.put(2, "two");

		assertThat(input, IsMapContainsEntries.containsEntries(
				hasEntry(is(0), containsString("e")),
				hasEntry(is(1), containsString("e")),
				hasEntry(2, "two")
		));
	}

	@Test
	public void testSimpleMismatch() {
		final Map<Integer, String> input = new HashMap<>();
		input.put(0, "zero");

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(input, IsMapContainsEntries.containsEntries(hasEntry(0, "hero")));
			}
		});

		assertThat(expectedFailure, hasMessage(containsString("not matched: <{0=zero}>")));
	}

	@Test
	public void testMissingEntrySingle() {
		final Map<Integer, String> input = new HashMap<>();

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(input, IsMapContainsEntries.containsEntries(hasEntry(0, "zero")));
			}
		});

		assertThat(expectedFailure, hasMessage(containsString("not matched: <{}>")));
	}

	@Test
	public void testMissingEntryMultiple() {
		final Map<Integer, String> input = new HashMap<>();
		input.put(0, "zero");
		input.put(2, "two");

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(input, IsMapContainsEntries.containsEntries(
						hasEntry(0, "zero"),
						hasEntry(1, "one"),
						hasEntry(2, "two")
				));
			}
		});

		assertThat(expectedFailure, hasMessage(containsString("not matched: <{0=zero, 2=two}>")));
	}

	@Test
	public void testExtraEntrySingle() {
		final Map<Integer, String> input = new HashMap<>();
		input.put(0, "zero");

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(input, IsMapContainsEntries.containsEntries());
			}
		});

		assertThat(expectedFailure, hasMessage(containsString("extra entries in <{0=zero}>")));
	}

	@Test
	public void testExtraEntryMultiple() {
		final Map<Integer, String> input = new HashMap<>();
		input.put(0, "zero");
		input.put(1, "one");
		input.put(2, "two");

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(input, IsMapContainsEntries.containsEntries(
						hasEntry(0, "zero"),
						hasEntry(2, "two")
				));
			}
		});

		assertThat(expectedFailure, hasMessage(containsString("extra entries in <{0=zero, 1=one, 2=two}>")));
	}
}
