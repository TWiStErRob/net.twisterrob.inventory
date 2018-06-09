package net.twisterrob.test.hamcrest;

import org.hamcrest.*;

public class MatcherTestHelpers {

	public static final String TRUE_MATCHER_DESCRIPTION = "true matcher";

	public static final Matcher<StackTraceElement[]> TRUE_MATCHER = new BaseMatcher<StackTraceElement[]>() {
		@Override public boolean matches(Object item) {
			return true;
		}
		@Override public void describeTo(Description description) {
			description.appendText(TRUE_MATCHER_DESCRIPTION);
		}
	};

	public static final String FALSE_MATCHER_DESCRIPTION = "false matcher";

	public static final Matcher<StackTraceElement[]> FALSE_MATCHER = new BaseMatcher<StackTraceElement[]>() {
		@Override public boolean matches(Object item) {
			return false;
		}
		@Override public void describeTo(Description description) {
			description.appendText(FALSE_MATCHER_DESCRIPTION);
		}
	};
}
