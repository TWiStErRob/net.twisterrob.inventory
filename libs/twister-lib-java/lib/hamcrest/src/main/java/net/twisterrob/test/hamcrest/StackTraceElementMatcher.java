package net.twisterrob.test.hamcrest;

import javax.annotation.Nonnull;

import org.hamcrest.*;

public class StackTraceElementMatcher {

	private static class StackTraceElementMethodMatcher extends TypeSafeDiagnosingMatcher<StackTraceElement> {

		private final @Nonnull Matcher<String> methodNameMatcher;

		public StackTraceElementMethodMatcher(@Nonnull Matcher<String> methodNameMatcher) {
			this.methodNameMatcher = methodNameMatcher;
		}

		@Override public void describeTo(Description description) {
			description.appendText("stack trace element with method ").appendDescriptionOf(methodNameMatcher);
		}

		@Override protected boolean matchesSafely(StackTraceElement item, Description mismatchDescription) {
			boolean matches = methodNameMatcher.matches(item.getMethodName());
			if (!matches) {
				mismatchDescription.appendText("method was ").appendValue(item.getMethodName())
				                   .appendText(" in ").appendValue(item);
			}
			return matches;
		}
	}

	static @Nonnull Matcher<StackTraceElement> stackMethod(final @Nonnull Matcher<String> methodNameMatcher) {
		return new StackTraceElementMethodMatcher(methodNameMatcher);
	}

	private static class StackTraceElementClassMatcher extends TypeSafeDiagnosingMatcher<StackTraceElement> {

		private final @Nonnull Matcher<String> classNameMatcher;

		public StackTraceElementClassMatcher(@Nonnull Matcher<String> classNameMatcher) {
			this.classNameMatcher = classNameMatcher;
		}

		@Override public void describeTo(Description description) {
			description.appendText("stack trace element with class ").appendDescriptionOf(classNameMatcher);
		}

		@Override protected boolean matchesSafely(StackTraceElement item, Description mismatchDescription) {
			boolean matches = classNameMatcher.matches(item.getClassName());
			if (!matches) {
				mismatchDescription.appendText("class was ").appendValue(item.getClassName())
				                   .appendText(" in ").appendValue(item);
			}
			return matches;
		}
	}

	static @Nonnull Matcher<StackTraceElement> stackClass(final @Nonnull Matcher<String> classNameMatcher) {
		return new StackTraceElementClassMatcher(classNameMatcher);
	}
}
