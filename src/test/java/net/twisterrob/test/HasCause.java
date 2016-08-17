package net.twisterrob.test;

import java.util.*;

import org.hamcrest.*;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;

@TargetApi(VERSION_CODES.KITKAT)
public class HasCause extends TypeSafeDiagnosingMatcher<Throwable> {
	public static final String NL = System.lineSeparator();

	private final Matcher<Throwable> matcher;

	public HasCause(Matcher<Throwable> matcher) {
		if (matcher == null) {
			throw new NullPointerException("Expected cause matcher cannot be null.");
		}
		this.matcher = matcher;
	}

	@Override protected boolean matchesSafely(Throwable item, Description mismatchDescription) {
		List<Throwable> causes = new LinkedList<>();
		while (item != null) {
			if (matcher.matches(item)) {
				return true;
			}
			causes.add(item);
			item = item.getCause();
		}
		mismatchDescription.appendValueList("was nested exceptions:" + NL, NL, "", causes);
		return false;
	}

	@Override public void describeTo(Description description) {
		matcher.describeTo(description);
	}

	public static Matcher<? super Throwable> hasCause(final Throwable ex) {
		if (ex == null) {
			throw new NullPointerException("Expected exception must not be null.");
		}
		return hasCause(Matchers.sameInstance(ex));
	}

	public static HasCause hasCause(Matcher<Throwable> matcher) {
		return new HasCause(matcher);
	}
}
