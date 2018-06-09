package net.twisterrob.test.hamcrest;

import java.util.Random;

import org.hamcrest.*;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WrappingMatcherTest {

	@SuppressWarnings("unchecked")
	Matcher<Object> mockWrappedMatcher = mock(Matcher.class);

	WrappingMatcher<Object> sut = new WrappingMatcher<>(mockWrappedMatcher);

	@After public void tearDown() {
		verifyNoMoreInteractions(mockWrappedMatcher);
	}

	@Test public void testMatches() {
		Object fixtObject = new Object();
		boolean fixtMatches = new Random().nextBoolean();
		when(mockWrappedMatcher.matches(fixtObject)).thenReturn(fixtMatches);

		boolean result = sut.matches(fixtObject);

		assertEquals(fixtMatches, result);
		verify(mockWrappedMatcher).matches(fixtObject);
	}

	@Test public void testDescribeMismatch() {
		Object fixtObject = new Object();
		Description fixtDescription = mock(Description.class);

		sut.describeMismatch(fixtObject, fixtDescription);

		verify(mockWrappedMatcher).describeMismatch(fixtObject, fixtDescription);
	}

	@Test public void testDescribeTo() {
		Description fixtDescription = mock(Description.class);

		sut.describeTo(fixtDescription);

		verify(mockWrappedMatcher).describeTo(fixtDescription);
	}

	@Ignore("Cannot do it yet, PowerMock doesn't work")
	@Test public void testToString() {
		String fixtString = "toString";
		when(mockWrappedMatcher.toString()).thenReturn(fixtString);

		String result = sut.toString();

		assertEquals(fixtString, result);
		//noinspection ResultOfMethodCallIgnored
		verify(mockWrappedMatcher).toString();
	}

	@Ignore("Cannot do it yet, PowerMock doesn't work")
	@Test public void testEqualsWrappingMatcher() {
		@SuppressWarnings("unchecked")
		Matcher<Object> mockOtherWrappedMatcher = mock(Matcher.class);
		WrappingMatcher<Object> otherWrappingMatcher = new WrappingMatcher<>(mockOtherWrappedMatcher);
		boolean fixtEquals = new Random().nextBoolean();
		doReturn(fixtEquals).when(mockWrappedMatcher).equals(mockOtherWrappedMatcher);

		boolean result = sut.equals(otherWrappingMatcher);

		assertEquals(fixtEquals, result);
		//noinspection ResultOfMethodCallIgnored
		verify(mockWrappedMatcher).equals(mockOtherWrappedMatcher);
	}

	@Ignore("Cannot do it yet, PowerMock doesn't work")
	@Test public void testEqualsSomethingElse() {
		Object fixtObject = new Object();
		boolean fixtEquals = new Random().nextBoolean();
		doReturn(fixtEquals).when(mockWrappedMatcher).equals(fixtObject);

		boolean result = sut.equals(fixtObject);

		assertEquals(fixtEquals, result);
		//noinspection ResultOfMethodCallIgnored
		verify(mockWrappedMatcher).equals(fixtObject);
	}

	@Ignore("Cannot do it yet, PowerMock doesn't work")
	@Test public void testHashCode() {
		int fixtHash = 12345678;
		when(mockWrappedMatcher.hashCode()).thenReturn(fixtHash);

		int result = sut.hashCode();

		assertEquals(fixtHash, result);
		//noinspection ResultOfMethodCallIgnored
		verify(mockWrappedMatcher).hashCode();
	}
}
