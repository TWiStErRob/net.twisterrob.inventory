package net.twisterrob.test.hamcrest;

import java.util.Arrays;
import java.util.zip.ZipEntry;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.support.annotation.NonNull;

public class Matchers {
	public static @NonNull Matcher<Class<?>> hasConstant(String constantName, Matcher<?> valueMatcher) {
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
		return new FeatureMatcher<ZipEntry, String>(nameMatchers, "zip entry name", "entry name") {
			@Override protected String featureValueOf(ZipEntry actual) {
				return actual.getName();
			}
		};
	}
}
