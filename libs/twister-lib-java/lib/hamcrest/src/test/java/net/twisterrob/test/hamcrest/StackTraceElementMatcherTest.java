package net.twisterrob.test.hamcrest;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.junit.rules.TestName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class StackTraceElementMatcherTest {

	@Rule public TestName name = new TestName();

	@Test public void testMethodMatchPasses() {
		StackTraceElement thisMethodElement = new Exception("test").getStackTrace()[0];
		String currentMethod = this.name.getMethodName();
		assertThat(thisMethodElement, StackTraceElementMatcher.stackMethod(equalTo(currentMethod)));
	}

	@Test public void testMethodMatchFailureMessage() {
		final String nonExistentMethodName = "nonExistentMethod";
		final StackTraceElement thisMethodElement = new Exception("test").getStackTrace()[0];
		String currentMethod = this.name.getMethodName();

		AssertionError expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(thisMethodElement, StackTraceElementMatcher.stackMethod(equalTo(nonExistentMethodName)));
			}
		});
		//Expected: stack trace element with method "nonExistentMethod"
		//     but: method was "testMethodMatchFailureMessage" in <...>

		assertThat(expectedFailure, hasMessage(allOf(
				containsString("stack trace element with method \"" + nonExistentMethodName + "\""),
				containsString("method was \"" + currentMethod + "\""),
				containsString(thisMethodElement.toString())
		)));
	}

	@Test public void testClassMatchPasses() {
		StackTraceElement thisMethodElement = new Exception("test").getStackTrace()[0];
		String currentClass = getClass().getName();

		assertThat(thisMethodElement, StackTraceElementMatcher.stackClass(equalTo(currentClass)));
	}

	@Test public void testClassMatchFailureMessage() {
		final String nonExistentClassName = "net.twisterrob.test.Fake$NonExistent";
		final StackTraceElement thisMethodElement = new Exception("test").getStackTrace()[0];
		String currentClass = getClass().getName();

		AssertionError expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertThat(thisMethodElement, StackTraceElementMatcher.stackClass(equalTo(nonExistentClassName)));
			}
		});
		//Expected: stack trace element with class "net.twisterrob.test.Fake$NonExistent"
		//     but: class was "net.twisterrob.test.hamcrest.StackTraceElementMatcherTest" in <...>

		assertThat(expectedFailure, hasMessage(allOf(
				containsString("stack trace element with class \"" + nonExistentClassName + "\""),
				containsString("class was \"" + currentClass + "\""),
				containsString(thisMethodElement.toString())
		)));
	}
}
