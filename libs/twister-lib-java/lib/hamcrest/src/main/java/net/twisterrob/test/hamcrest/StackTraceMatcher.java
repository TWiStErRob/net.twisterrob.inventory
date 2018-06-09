package net.twisterrob.test.hamcrest;

import javax.annotation.Nonnull;

import org.hamcrest.*;

public class StackTraceMatcher extends TypeSafeDiagnosingMatcher<Throwable> {

	private final @Nonnull Matcher<? super StackTraceElement[]> matcher;

	public StackTraceMatcher(@Nonnull Matcher<? super StackTraceElement[]> matcher) {
		this.matcher = matcher;
	}

	@Override public void describeTo(Description description) {
		matcher.describeTo(description);
	}

	@Override protected boolean matchesSafely(final Throwable item, Description mismatchDescription) {
		StackTraceElement[] trace = item.getStackTrace();
		boolean matches = matcher.matches(trace);
		if (!matches) {
			mismatchDescription.appendText("didn't match stack trace of ")
			                   .appendDescriptionOf(new SelfDescribingThrowable(item));
		}
		return matches;
	}

	static StackTraceMatcher hasStackTrace(@Nonnull Matcher<? super StackTraceElement[]> matcher) {
		return new StackTraceMatcher(matcher);
	}
}
