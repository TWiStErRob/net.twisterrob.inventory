package net.twisterrob.test.hamcrest;

import java.util.Arrays;
import java.util.zip.ZipEntry;

import javax.annotation.*;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

public class Matchers {
	public static @Nonnull Matcher<Class<?>> hasConstant(String constantName, Matcher<?> valueMatcher) {
		return new ConstantMatcher(constantName, valueMatcher);
	}

	public static <T> ExactlyOneOf<T> exactlyOneOf(Iterable<Matcher<? super T>> matchers) {
		return new ExactlyOneOf<>(matchers);
	}

	@SafeVarargs
	public static <T> ExactlyOneOf<T> exactlyOneOf(Matcher<? super T>... matchers) {
		return exactlyOneOf(Arrays.asList(matchers));
	}

	public static Matcher<ZipEntry> zipEntryWithName(String name) {
		return zipEntryWithName(is(name));
	}
	public static Matcher<ZipEntry> zipEntryWithName(Matcher<String> nameMatchers) {
		return new FeatureMatcher<ZipEntry, String>(nameMatchers, "zip entry with name", "entry name") {
			@Override protected String featureValueOf(ZipEntry actual) {
				return actual.getName();
			}
		};
	}

	public static <T extends Throwable> Matcher<T> hasCause(final @Nullable Throwable cause) {
		return hasCause(sameInstance(cause));
	}
	@SuppressWarnings("deprecation")
	public static <T extends Throwable> Matcher<T> hasCause(final @Nonnull Matcher<?> matcher) {
		return org.junit.internal.matchers.ThrowableCauseMatcher.hasCause(matcher);
	}

	public static <T extends Throwable> Matcher<Throwable> containsCause(final @Nonnull Matcher<Throwable> matcher) {
		return HasCause.hasCause(matcher);
	}
	public static <T extends Throwable> Matcher<? super Throwable> containsCause(final @Nullable Throwable cause) {
		return HasCause.hasCause(cause);
	}

	public static <T extends Throwable> Matcher<T> hasMessage(final @Nullable String message) {
		return hasMessage(equalTo(message));
	}
	public static <T extends Throwable> Matcher<T> hasMessage(final @Nonnull Matcher<String> matcher) {
		return org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage(matcher);
	}
}
