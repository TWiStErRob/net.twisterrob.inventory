package net.twisterrob.test.hamcrest;

import org.hamcrest.*;

public class WrappingMatcher<T> implements Matcher<T> {
	private final Matcher<T> matcher;
	public WrappingMatcher(Matcher<T> matcher) {
		this.matcher = matcher;
	}
	@Override public boolean matches(Object item) {
		return matcher.matches(item);
	}
	@Override public void describeMismatch(Object item, Description description) {
		matcher.describeMismatch(item, description);
	}
	@Override public void describeTo(Description description) {
		matcher.describeTo(description);
	}
	@Override public String toString() {
		return matcher.toString();
	}
	@Override public int hashCode() {
		return matcher.hashCode();
	}
	@Override public boolean equals(Object obj) {
		if (obj instanceof WrappingMatcher) {
			return matcher.equals(((WrappingMatcher<?>)obj).matcher);
		} else {
			return matcher.equals(obj);
		}
	}
	@SuppressWarnings("deprecation")
	@Override public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
		// want to break if interface changes, otherwise not all methods may be delegated.
	}
}
