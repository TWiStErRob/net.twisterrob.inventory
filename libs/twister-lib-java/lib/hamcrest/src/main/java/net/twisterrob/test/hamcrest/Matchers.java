package net.twisterrob.test.hamcrest;

import java.util.*;
import java.util.zip.ZipEntry;

import javax.annotation.*;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
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

	public static Matcher<Throwable> containsCause(final @Nonnull Matcher<Throwable> matcher) {
		return HasCause.hasCause(matcher);
	}
	public static Matcher<? super Throwable> containsCause(final @Nullable Throwable cause) {
		return HasCause.hasCause(cause);
	}

	@SafeVarargs
	public static Matcher<Throwable> containsStackTrace(final @Nonnull Matcher<StackTraceElement>... matchers) {
		Collection<Matcher<? super Throwable>> conditions = new ArrayList<>(matchers.length);
		for (Matcher<StackTraceElement> matcher : matchers) {
			conditions.add(containsCause(hasStackTraceElement(matcher)));
		}

		//noinspection SSBasedInspection it's an allOf overload that has 1 argument
		return allOf(conditions);
	}

	@SuppressWarnings("unchecked")
	public static Matcher<Throwable> hasStackTraceElement(final @Nonnull Matcher<? super StackTraceElement> matcher) {
		return hasStackTrace(hasItemInArray(matcher));
	}

	public static Matcher<Throwable> hasStackTrace(final @Nonnull Matcher<? super StackTraceElement[]> matcher) {
		return StackTraceMatcher.hasStackTrace(matcher);
	}
	@SafeVarargs
	public static Matcher<Throwable> hasStackTrace(final @Nonnull Matcher<StackTraceElement>... matchers) {
		Collection<Matcher<? super Throwable>> conditions = new ArrayList<>(matchers.length);
		for (Matcher<StackTraceElement> matcher : matchers) {
			conditions.add(hasStackTraceElement(matcher));
		}

		//noinspection SSBasedInspection it's an allOf overload that has 1 argument
		return allOf(conditions);
	}

	public static @Nonnull Matcher<StackTraceElement> stackMethod(final @Nonnull String method) {
		return stackMethod(equalTo(method));
	}
	public static @Nonnull Matcher<StackTraceElement> stackMethod(final @Nonnull Matcher<String> method) {
		return StackTraceElementMatcher.stackMethod(method);
	}
	public static @Nonnull Matcher<StackTraceElement> stackClass(final @Nonnull String className) {
		return stackClass(equalTo(className));
	}
	public static @Nonnull Matcher<StackTraceElement> stackClass(final @Nonnull Matcher<String> className) {
		return StackTraceElementMatcher.stackClass(className);
	}

	public static <T extends Throwable> Matcher<T> hasMessage(final @Nullable String message) {
		return hasMessage(equalTo(message));
	}
	public static <T extends Throwable> Matcher<T> hasMessage(final @Nonnull Matcher<String> matcher) {
		return org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage(matcher);
	}
}
