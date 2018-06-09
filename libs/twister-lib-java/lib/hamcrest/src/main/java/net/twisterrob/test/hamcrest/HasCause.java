package net.twisterrob.test.hamcrest;

import javax.annotation.Nonnull;

import org.hamcrest.*;
import org.hamcrest.Matchers;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;

@TargetApi(VERSION_CODES.KITKAT)
public class HasCause extends TypeSafeDiagnosingMatcher<Throwable> {
	public static final String NL = System.lineSeparator();

	private final @Nonnull Matcher<Throwable> matcher;

	public HasCause(Matcher<Throwable> matcher) {
		if (matcher == null) {
			throw new NullPointerException("Expected cause matcher cannot be null.");
		}
		this.matcher = matcher;
	}

	@Override protected boolean matchesSafely(Throwable item, Description mismatchDescription) {
		while (item != null) {
			if (matcher.matches(item)) {
				return true;
			} else {
				mismatchDescription.appendText("cause didn't match" + NL);
				matcher.describeMismatch(item, mismatchDescription);
			}
			item = item.getCause();
		}
		return false;
	}

	@Override public void describeTo(Description description) {
		matcher.describeTo(description);
	}

	static Matcher<? super Throwable> hasCause(final Throwable ex) {
		if (ex == null) {
			throw new NullPointerException("Expected exception must not be null.");
		}
		return hasCause(Matchers.sameInstance(ex));
	}

	static HasCause hasCause(@Nonnull Matcher<Throwable> matcher) {
		return new HasCause(matcher);
	}
}
