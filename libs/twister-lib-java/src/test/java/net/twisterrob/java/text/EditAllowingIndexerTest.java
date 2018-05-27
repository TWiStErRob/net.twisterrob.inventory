package net.twisterrob.java.text;

import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import net.twisterrob.java.text.Indexer.MatchResult;

public class EditAllowingIndexerTest {
	@Ignore("debug helper only")
	@Test public void debugDelete() {
		EditAllowingIndexer<String> indexer = new EditAllowingIndexer<>(2);
		indexer.add("abcd", "test");
		Collection<MatchResult<String>> matches = indexer.match("abxcd"); // x deleted
		System.out.println(matches);
	}

	@Ignore("debug helper only")
	@Test public void debugInsert() {
		EditAllowingIndexer<String> indexer = new EditAllowingIndexer<>(2);
		indexer.add("abxcd", "test");
		Collection<MatchResult<String>> matches = indexer.match("abcd"); // x inserted
		System.out.println(matches);
	}

	@Ignore("debug helper only")
	@Test public void debugReplace() {
		EditAllowingIndexer<String> indexer = new EditAllowingIndexer<>(2);
		indexer.add("abycd", "test");
		Collection<MatchResult<String>> matches = indexer.match("abxcd"); // x replaced to y
		System.out.println(matches);
	}

	@Ignore("debug helper only")
	@Test public void debugSwap() {
		EditAllowingIndexer<String> indexer = new EditAllowingIndexer<>(2);
		indexer.add("abcd", "test");
		Collection<MatchResult<String>> matches = indexer.match("acbd"); // b and c swapped in middle
		System.out.println(matches);
	}

	@Test public void testDistances() {
		EditAllowingIndexer<String> indexer = new EditAllowingIndexer<>(2);
		indexer.add("abc", "abc");
		indexer.add("cba", "abc");
		indexer.add("abd", "abc");
		indexer.add("bac", "abc");
		indexer.add("def", "def");
		Collection<MatchResult<String>> matches = indexer.match("abc");
		assertThat(matches, Matchers.hasSize(4));
		for (MatchResult<String> match : matches) {
			switch (match.search.toString()) {
				case "abc":
					assertEquals(match.toString(), 0, match.distance);
					break;
				case "abd":
					assertEquals(match.toString(), 1, match.distance);
					break;
				case "bac":
					assertEquals(match.toString(), 1, match.distance);
					break;
				case "cba":
					assertEquals(match.toString(), 2, match.distance);
					break;
				default:
					fail("Unexpected match:" + match);
			}
		}
	}
}
