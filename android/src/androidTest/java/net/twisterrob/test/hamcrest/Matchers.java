package net.twisterrob.test.hamcrest;

import org.hamcrest.Matcher;

import android.support.annotation.NonNull;

public class Matchers {
	public static @NonNull Matcher<Class<?>> hasConstant(String constantName, Matcher<?> valueMatcher) {
		return new ConstantMatcher(constantName, valueMatcher);
	}
}
