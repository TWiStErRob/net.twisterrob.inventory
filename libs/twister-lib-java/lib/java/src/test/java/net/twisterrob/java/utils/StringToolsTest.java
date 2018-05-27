package net.twisterrob.java.utils;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class StringToolsTest {

	private static final String EMPTY = "";
	private static final String TEXT = "abcdeb";
	private static final String MATCH = "b";
	private static final String MATCH_LONG = "deb";
	private static final String MISMATCH = "f";
	private static final String MISMATCH_LONG = "fghi";

	@Test public void endsWithEmptyBuilder() {
		assertEquals(EMPTY.endsWith(MATCH), StringTools.endsWith(new StringBuilder(EMPTY), MATCH));
		assertEquals(EMPTY.endsWith(MATCH_LONG), StringTools.endsWith(new StringBuilder(EMPTY), MATCH_LONG));
	}

	@Test public void endsWithEmptySearch() {
		assertEquals(EMPTY.endsWith(EMPTY), StringTools.endsWith(new StringBuilder(EMPTY), EMPTY));
		assertEquals(TEXT.endsWith(EMPTY), StringTools.endsWith(new StringBuilder(TEXT), EMPTY));
	}

	@Test public void endsWithMatch() {
		assertEquals(TEXT.endsWith(MATCH), StringTools.endsWith(new StringBuilder(TEXT), MATCH));
		assertEquals(TEXT.endsWith(MATCH_LONG), StringTools.endsWith(new StringBuilder(TEXT), MATCH_LONG));
	}

	@Test public void endsWithMisMatch() {
		assertEquals(TEXT.endsWith(MISMATCH), StringTools.endsWith(new StringBuilder(TEXT), MISMATCH));
		assertEquals(TEXT.endsWith(MISMATCH_LONG), StringTools.endsWith(new StringBuilder(TEXT), MISMATCH_LONG));
	}

	@Test public void hashString() {
		Object obj = new Object();
		String hash = StringTools.hashString(obj);
		assertThat(obj.toString(), containsString(hash));
		assertThat(obj.toString(), matchesPattern("^java\\.lang\\.Object@" + hash + "$"));
	}

	@Test public void hashStringNull() {
		assertEquals("0", StringTools.hashString(null));
	}
}
