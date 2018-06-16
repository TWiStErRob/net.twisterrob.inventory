package net.twisterrob.test.hamcrest;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.junit.rules.TestName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;
import static org.junit.Assert.*;

import net.twisterrob.java.utils.ObjectTools;

import static net.twisterrob.java.utils.ReflectionTools.*;
import static net.twisterrob.test.hamcrest.Matchers.*;

public class MatchersTest {

	@Rule public final TestName name = new TestName();

	@Test public void testTestMethodIsInStackTraceContains() {
		Exception exception = new Exception();

		assertThat(exception, containsStackTrace(stackMethod(name.getMethodName())));
	}

	@Test public void testTestMethodIsInStackTraceContainsNested() {
		Exception exception = new Exception(createException());

		assumeThat(exception, not(hasStackTrace(stackMethod("createException"))));
		assertThat(exception, containsStackTrace(stackMethod("createException")));
	}

	@Test public void testTestMethodIsInStackTraceDirectElement() {
		Exception exception = new Exception();

		assertThat(exception, hasStackTraceElement(stackMethod(name.getMethodName())));
	}

	@Test public void testTestMethodIsInStackTraceDirect() {
		Exception exception = new Exception();

		assertThat(exception, hasStackTrace(stackMethod(name.getMethodName())));
	}

	@Test public void testContainsStackTraceFailsIfNotFound() {
		IllegalStateException innerMost = new IllegalStateException("state message");
		IllegalArgumentException outer = new IllegalArgumentException("argument message", innerMost);
		final Exception exception = new Exception(outer);

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(exception, containsStackTrace(stackMethod("non-existent method")));
			}
		});

		assertThat(expectedFailure, hasMessage(containsString("non-existent method")));
		assertThat(expectedFailure, hasMessage(containsString(ObjectTools.getFullStackTrace(clearCause(exception)))));
		assertThat(expectedFailure, hasMessage(containsString(ObjectTools.getFullStackTrace(clearCause(outer)))));
		assertThat(expectedFailure, hasMessage(containsString(ObjectTools.getFullStackTrace(clearCause(innerMost)))));
	}

	/**
	 * Just creates an exception, the method name is used in tests.
	 */
	private static Throwable createException() {
		return new Exception();
	}
}
