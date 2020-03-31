package net.twisterrob.test.hamcrest;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.annotation.*;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import net.twisterrob.java.io.IOTools;

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

	@SafeVarargs
	public static <K, V> Matcher<Map<? extends K, ? extends V>> containsEntries(
			Matcher<? super Map<? extends K, ? extends V>>... matchers) {
		return IsMapContainsEntries.containsEntries(matchers);
	}
	public static <K, V> Matcher<Map<? extends K, ? extends V>> containsEntries(
			Collection<Matcher<? super Map<? extends K, ? extends V>>> matchers) {
		return IsMapContainsEntries.containsEntries(matchers);
	}

	public static Matcher<ZipFile> hasEntry(final Matcher<ZipEntry> entryMatcher) {
		return new TypeSafeMatcher<ZipFile>() {
			private final Matcher<Iterable<? super ZipEntry>> matcher = hasItem(entryMatcher);
			@Override public void describeTo(Description description) {
				matcher.describeTo(description);
			}
			@Override public void describeMismatchSafely(ZipFile item, Description description) {
				description.appendText("Didn't match any entry in ")
				           .appendValue(item)
				           .appendText("\n");
				for (ZipEntry entry : entries(item)) {
					entryMatcher.describeMismatch(entry, description);
					description.appendText("\n");
				}
			}
			@Override public boolean matchesSafely(ZipFile item) {
				return matcher.matches(entries(item));
			}
			private ArrayList<? extends ZipEntry> entries(ZipFile item) {
				return Collections.list(item.entries());
			}
		};
	}
	public static Matcher<ZipEntry> hasNonEmptyEntry(String name) {
		return allOf(
				zipEntryWithName(name),
				zipEntryWithSize(greaterThan(0L))
		);
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
	public static Matcher<ZipEntry> zipEntryWithSize(Matcher<Long> nameMatchers) {
		return new FeatureMatcher<ZipEntry, Long>(nameMatchers, "zip entry with uncompressed size", "entry uncompressed size") {
			@Override protected Long featureValueOf(ZipEntry actual) {
				return actual.getSize();
			}
		};
	}
	
	public static Matcher<ZipEntry> zipEntryWithContent(ZipFile zip, String contents) {
		try {
			return zipEntryWithContent(zip, equalTo(contents.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	/**
	 * Note: don't use {@code not(zipEntryWithContent(zip, ...))},
	 * do {@code zipEntryWithContent(zip, not(...))} instead.
	 */
	public static Matcher<ZipEntry> zipEntryWithContent(final ZipFile zip, Matcher<byte[]> contentMatcher) {
		return new FeatureMatcher<ZipEntry, byte[]>(contentMatcher, "zip entry with contents", "entry contents") {
			@Override protected boolean matchesSafely(ZipEntry actual, Description mismatch) {
				boolean matches = super.matchesSafely(actual, Description.NONE);
				if (!matches) {
					mismatch.appendText("but was ");
					try {
						// Intentionally not supporting UTF-8, so all characters are visible.
						mismatch.appendValue(new String(featureValueOf(actual), "ISO-8859-1"));
					} catch (UnsupportedEncodingException e) {
						throw new IllegalStateException(e);
					}
					mismatch.appendText(" = ");
					mismatch.appendValue(featureValueOf(actual));
				}
				return matches;
			}
			@Override protected byte[] featureValueOf(ZipEntry actual) {
				try {
					return IOTools.readBytes(zip.getInputStream(actual));
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
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
