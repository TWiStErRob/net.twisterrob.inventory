package net.twisterrob.test;

import java.io.FileNotFoundException;

import org.hamcrest.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class HasCauseTest {
	@Rule public ExpectedException thrown = ExpectedException.none();

	@Test public void matchesSelf() {
		FileNotFoundException ex = new FileNotFoundException("test");
		assertThat(ex, HasCause.hasCause(ex));
	}

	@Test public void matchesCause() {
		FileNotFoundException ex = new FileNotFoundException("test");
		Exception caused = new Exception(ex);
		assertThat(caused, HasCause.hasCause(ex));
	}

	@Test public void matchesNestedCause() {
		FileNotFoundException ex = new FileNotFoundException("test");
		Exception nested = new Exception(new Exception(new Exception(ex)));
		assertThat(nested, HasCause.hasCause(ex));
	}

	@Test public void failsSimilar() {
		FileNotFoundException ex = new FileNotFoundException("test");
		FileNotFoundException similar = new FileNotFoundException("test");
		assertThat(similar, not(HasCause.hasCause(ex)));
	}

	@Test public void failsSimilarCause() {
		FileNotFoundException ex = new FileNotFoundException("test");
		FileNotFoundException similar = new FileNotFoundException("test");
		assertThat(new Exception(similar), not(HasCause.hasCause(ex)));
	}

	@Test public void failsNull() {
		FileNotFoundException ex = new FileNotFoundException("test");
		assertThat(null, not(HasCause.hasCause(ex)));
	}

	@Test public void throwsNullExpectation() {
		thrown.expect(NullPointerException.class);
		HasCause.hasCause((Throwable)null);
	}

	@Test public void throws1NullExpectation() {
		thrown.expect(NullPointerException.class);
		HasCause.hasCause((Matcher<Throwable>)null);
	}

	@Test public void hasSameDescription() {
		final String failureDescription = "description of the matcher that the cause should match";
		Matcher<Throwable> matcher = new BaseMatcher<Throwable>() {
			@Override public boolean matches(Object item) {
				return false; // don't match anything (forces describeTo)
			}
			@Override public void describeTo(Description description) {
				description.appendText(failureDescription);
			}
		};

		thrown.expectMessage(failureDescription);
		assertThat(null, HasCause.hasCause(matcher));
	}
}
