package net.twisterrob.test.hamcrest;

import java.util.*;

import org.hamcrest.*;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.internal.NullSafety;

/**
 * Analogous version of {@link IsIterableContainingInAnyOrder}, but for {@link Map}s.
 *
 * @see org.hamcrest.Matchers#containsInAnyOrder
 */
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
	static <K, V> Matcher<Map<? extends K, ? extends V>> containsEntries(
			Matcher<? super Map<? extends K, ? extends V>>... matchers) {
		List<Matcher<? super Map<? extends K, ? extends V>>> matchers1 = NullSafety.nullSafe(matchers);
		return containsEntries(matchers1);
	}
	static <K, V> Matcher<Map<? extends K, ? extends V>> containsEntries(
			Collection<Matcher<? super Map<? extends K, ? extends V>>> matchers) {
		return new IsMapContainsEntries<>(matchers);
	}

	private static class Matching<M> {
		private final Collection<Matcher<? super M>> matchers;
		private final Description mismatchDescription;
		private int matched = 0;

		public Matching(Collection<Matcher<? super M>> matchers, Description mismatchDescription) {
			this.matchers = new ArrayList<>(matchers);
			this.mismatchDescription = mismatchDescription;
		}

		public boolean matches(M item) {
			if (matchers.isEmpty()) {
				mismatchDescription.appendText("no match for: ").appendValue(item);
				return false;
			}
			return isMatched(item);
		}

		public boolean isFinished(Map<?, ?> items) {
			if (matchers.isEmpty()) {
				if (matched != items.size()) {
					mismatchDescription.appendText("extra entries in ").appendValue(items);
					return false;
				}
				return true;
			}
			mismatchDescription
					.appendText("no entry matches: ").appendList("", ", ", "", matchers)
					.appendText(" in ").appendValue(items);
			return false;
		}

		private boolean isMatched(M map) {
			for (Matcher<? super M> matcher : matchers) {
				if (matcher.matches(map)) {
					matchers.remove(matcher);
					matched++;
					return true;
				}
			}
			mismatchDescription.appendText("not matched: ").appendValue(map);
			return false;
		}
	}
}
