package net.twisterrob.java.collections;

import java.util.*;

import org.junit.Test;

import static org.junit.Assert.*;

import net.twisterrob.java.collections.FilteredIterator.Filter;

public class FilteredIteratorTest {
	@Test public void testEmptyAlwaysEmpty() {
		Iterator<String> it = testEmptyCore();
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}
	@Test(expected = NoSuchElementException.class)
	public void testEmptyThrows() {
		Iterator<String> it = testEmptyCore();
		it.next();
	}
	private Iterator<String> testEmptyCore() {
		Iterator<String> it = new FilteredIterator<>(Collections.<String>emptyList().iterator(), TRUE);
		assertFalse(it.hasNext());
		return it;
	}

	@Test public void testSingular() {
		Iterator<String> it = testSingularCore();
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}
	@Test(expected = NoSuchElementException.class)
	public void testSingularThrowsAfterEnd() {
		Iterator<String> it = testSingularCore();
		it.next();
	}
	private Iterator<String> testSingularCore() {
		Iterator<String> it = new FilteredIterator<>(Collections.singleton("one").iterator(), TRUE);
		assertTrue(it.hasNext());
		assertEquals("one", it.next());
		assertFalse(it.hasNext());
		return it;
	}

	@Test public void testMany() {
		Iterator<String> it = testManyCore();
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}
	@Test(expected = NoSuchElementException.class)
	public void testManyThrowsAfterEnd() {
		Iterator<String> it = testManyCore();
		it.next();
	}
	private Iterator<String> testManyCore() {
		Iterator<String> it = new FilteredIterator<>(Arrays.asList("one", "two", "three", "four").iterator(), TRUE);
		assertTrue(it.hasNext());
		assertEquals("one", it.next());
		assertTrue(it.hasNext());
		assertEquals("two", it.next());
		assertTrue(it.hasNext());
		assertEquals("three", it.next());
		assertTrue(it.hasNext());
		assertEquals("four", it.next());
		assertFalse(it.hasNext());
		return it;
	}

	@Test public void testFilterAll() {
		Iterator<String> it = testFilterAllCore();
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}
	@Test(expected = NoSuchElementException.class)
	public void testFilterAllThrowsAfterEnd() {
		Iterator<String> it = testFilterAllCore();
		it.next();
	}
	private Iterator<String> testFilterAllCore() {
		Iterator<String> it = new FilteredIterator<>(Arrays.asList("one", "two", "three", "four").iterator(), FALSE);
		assertFalse(it.hasNext());
		return it;
	}

	@Test public void testFilterSome() {
		Iterator<String> it = testFilterSomeCore();
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}
	@Test(expected = NoSuchElementException.class)
	public void testFilterSomeThrowsAfterEnd() {
		Iterator<String> it = testFilterSomeCore();
		it.next();
	}
	private Iterator<String> testFilterSomeCore() {
		Iterator<String> it = new FilteredIterator<>(Arrays.asList("one", "two", "three", "four").iterator(),
				new Filter<String>() {
					@Override public boolean matches(String element) {
						return element.startsWith("t");
					}
				});
		assertTrue(it.hasNext());
		assertEquals("two", it.next());
		assertTrue(it.hasNext());
		assertEquals("three", it.next());
		assertFalse(it.hasNext());
		return it;
	}

	@Test public void testFilterStart() {
		Iterator<String> it = testFilterStartCore();
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}
	@Test(expected = NoSuchElementException.class)
	public void testFilterStartThrowsAfterEnd() {
		Iterator<String> it = testFilterStartCore();
		it.next();
	}
	private Iterator<String> testFilterStartCore() {
		Iterator<String> it = new FilteredIterator<>(Arrays.asList("one", "two", "three", "four").iterator(),
				new Filter<String>() {
					@Override public boolean matches(String element) {
						return element.startsWith("o");
					}
				});
		assertTrue(it.hasNext());
		assertEquals("one", it.next());
		assertFalse(it.hasNext());
		return it;
	}

	@Test public void testFilterEnd() {
		Iterator<String> it = testFilterEndCore();
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}
	@Test(expected = NoSuchElementException.class)
	public void testFilterEndThrowsAfterEnd() {
		Iterator<String> it = testFilterEndCore();
		it.next();
	}
	private Iterator<String> testFilterEndCore() {
		Iterator<String> it = new FilteredIterator<>(Arrays.asList("one", "two", "three", "four").iterator(),
				new Filter<String>() {
					@Override public boolean matches(String element) {
						return element.startsWith("f");
					}
				});
		assertTrue(it.hasNext());
		assertEquals("four", it.next());
		assertFalse(it.hasNext());
		return it;
	}

	@Test public void testFilterStartAndEnd() {
		Iterator<String> it = testFilterStartAndEndCore();
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}
	@Test(expected = NoSuchElementException.class)
	public void testFilterStartAndEndThrowsAfterEnd() {
		Iterator<String> it = testFilterStartAndEndCore();
		it.next();
	}
	private Iterator<String> testFilterStartAndEndCore() {
		Iterator<String> it = new FilteredIterator<>(Arrays.asList("one", "two", "three", "four").iterator(),
				new Filter<String>() {
					@Override public boolean matches(String element) {
						return !element.startsWith("t");
					}
				});
		assertTrue(it.hasNext());
		assertEquals("one", it.next());
		assertTrue(it.hasNext());
		assertEquals("four", it.next());
		assertFalse(it.hasNext());
		return it;
	}

	@Test public void testNoHasNext() {
		Iterator<String> it = new FilteredIterator<>(Arrays.asList("one", "two", "three", "four").iterator(), TRUE);
		assertEquals("one", it.next());
		assertEquals("two", it.next());
		assertEquals("three", it.next());
		assertEquals("four", it.next());
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}
	@Test(expected = NoSuchElementException.class)
	public void testNoHasNextFilterAll() {
		Iterator<String> it = new FilteredIterator<>(Arrays.asList("one", "two", "three", "four").iterator(), FALSE);
		it.next();
	}
	@Test public void testNoHasNextFilterSome() {
		Iterator<String> it = new FilteredIterator<>(Arrays.asList("one", "two", "three", "four").iterator(),
				new Filter<String>() {
					@Override public boolean matches(String element) {
						return element.startsWith("t");
					}
				});
		assertEquals("two", it.next());
		assertEquals("three", it.next());
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
	}

	private static final FilteredIterator.Filter<String> TRUE = new Filter<String>() {
		@Override public boolean matches(String element) {
			return true;
		}
	};
	private static final FilteredIterator.Filter<String> FALSE = new Filter<String>() {
		@Override public boolean matches(String element) {
			return false;
		}
	};
}
