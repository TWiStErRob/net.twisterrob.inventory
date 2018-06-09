package net.twisterrob.test.hamcrest;

import java.io.FileNotFoundException;

import org.hamcrest.*;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class HasCauseTest {

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
		assertThat(new Exception(ex), not(HasCause.hasCause(similar)));
	}

	@Test public void failsNull() {
		FileNotFoundException ex = new FileNotFoundException("test");
		assertThat(null, not(HasCause.hasCause(ex)));
	}

	@Test public void throwsNullExpectation() {
		assertThrows(NullPointerException.class, new ThrowingRunnable() {
			@Override public void run() {
				HasCause.hasCause((Throwable)null);
			}
		});
	}

	@Test public void throws1NullExpectation() {
		assertThrows(NullPointerException.class, new ThrowingRunnable() {
			@Override public void run() {
				HasCause.hasCause((Matcher<Throwable>)null);
			}
		});
	}

	@Test public void hasSameDescription() {
		final String failureDescription = "description of the matcher that the cause should match";
		final Matcher<Throwable> matcher = new BaseMatcher<Throwable>() {
			@Override public boolean matches(Object item) {
				return false; // don't match anything (forces describeTo)
			}
			@Override public void describeTo(Description description) {
				description.appendText(failureDescription);
			}
		};

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(new Throwable(), HasCause.hasCause(matcher));
			}
		});

		assertThat(expectedFailure, hasMessage(containsString(failureDescription)));
	}
}
