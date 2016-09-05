package net.twisterrob.test.hamcrest;

import org.hamcrest.*;

public class ExactlyOneOf<T> extends BaseMatcher<T> {
	private final Iterable<Matcher<? super T>> matchers;

	public ExactlyOneOf(Iterable<Matcher<? super T>> matchers) {
		this.matchers = matchers;
	}

	@Override public boolean matches(Object item) {
		boolean matched = false;
		for (Matcher<? super T> matcher : matchers) {
			if (matcher.matches(item)) {
				if (!matched) {
					matched = true;
				} else {
					return false; // matched two of the matchers
				}
			}
		}
		return matched;
	}

	@Override public void describeTo(Description description) {
		description.appendList("exactly one of (", " or ", ")", matchers);
	}
}
