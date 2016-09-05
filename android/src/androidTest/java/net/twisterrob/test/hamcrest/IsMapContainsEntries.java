package net.twisterrob.test.hamcrest;

import java.util.*;

import org.hamcrest.*;
import org.hamcrest.internal.NullSafety;

public class IsMapContainsEntries<K, V> extends TypeSafeDiagnosingMatcher<Map<? extends K, ? extends V>> {
	private final Collection<Matcher<? super Map<? extends K, ? extends V>>> matchers;

	public IsMapContainsEntries(Collection<Matcher<? super Map<? extends K, ? extends V>>> matchers) {
		this.matchers = matchers;
	}

	@Override protected boolean matchesSafely(Map<? extends K, ? extends V> items, Description mismatchDescription) {
		final Matching<Map<? extends K, ? extends V>> matching = new Matching<>(matchers, mismatchDescription);
		for (int i = 0; i < matchers.size(); i++) {
			if (!matching.matches(items)) {
				return false;
			}
		}

		return matching.isFinished(items);
	}

	@Override public void describeTo(Description description) {
		description.appendText("map with entries ")
		           .appendList("[", ", ", "]", matchers)
		           .appendText(" in any order");
	}

	@SafeVarargs
	public static <K, V> Matcher<Map<? extends K, ? extends V>> containsEntries(
			Matcher<? super Map<? extends K, ? extends V>>... matchers) {
		List<Matcher<? super Map<? extends K, ? extends V>>> matchers1 = NullSafety.nullSafe(matchers);
		return containsEntries(matchers1);
	}
	public static <K, V> Matcher<Map<? extends K, ? extends V>> containsEntries(
			List<Matcher<? super Map<? extends K, ? extends V>>> matchers) {
		return new IsMapContainsEntries<>(matchers);
	}

	private static class Matching<S> {
		private final Collection<Matcher<? super S>> matchers;
		private final Description mismatchDescription;

		public Matching(Collection<Matcher<? super S>> matchers, Description mismatchDescription) {
			this.matchers = new ArrayList<>(matchers);
			this.mismatchDescription = mismatchDescription;
		}

		public boolean matches(S item) {
			if (matchers.isEmpty()) {
				mismatchDescription.appendText("no match for: ").appendValue(item);
				return false;
			}
			return isMatched(item);
		}

		public boolean isFinished(Map<?, ?> items) {
			if (matchers.isEmpty()) {
				return true;
			}
			mismatchDescription
					.appendText("no item matches: ").appendList("", ", ", "", matchers)
					.appendText(" in ").appendValue(items);
			return false;
		}

		private boolean isMatched(S item) {
			for (Matcher<? super S> matcher : matchers) {
				if (matcher.matches(item)) {
					matchers.remove(matcher);
					return true;
				}
			}
			mismatchDescription.appendText("not matched: ").appendValue(item);
			return false;
		}
	}
}
