package net.twisterrob.test.hamcrest;

import java.lang.reflect.*;

import org.hamcrest.*;

import net.twisterrob.java.utils.ReflectionTools;

class ConstantMatcher extends TypeSafeDiagnosingMatcher<Class<?>> {
	private final String constantName;
	private final Matcher<?> valueMatcher;
	public ConstantMatcher(String constantName, Matcher<?> valueMatcher) {
		super(Class.class);
		this.constantName = constantName;
		this.valueMatcher = valueMatcher;
	}

	@Override protected boolean matchesSafely(Class<?> item, Description mismatchDescription) {
		Field field;
		try {
			field = ReflectionTools.findDeclaredField(item, constantName);
		} catch (NoSuchFieldException ex) {
			mismatchDescription.appendValue(ex);
			return false;
		}
		if (!Modifier.isStatic(field.getModifiers())) {
			mismatchDescription.appendText("non-static field ").appendValue(field);
			return false;
		}
		if (!Modifier.isFinal(field.getModifiers())) {
			mismatchDescription.appendText("non-final class field ").appendValue(field);
			return false;
		}
		Object value;
		try {
			field.setAccessible(true);
			value = field.get(null);
		} catch (IllegalAccessException ex) {
			mismatchDescription.appendValue(ex);
			return false;
		}
		if (!valueMatcher.matches(value)) {
			valueMatcher.describeMismatch(value, mismatchDescription);
			return false;
		}
		return true;
	}

	@Override public void describeTo(Description description) {
		description.appendText("static final class member named ").appendValue(constantName)
		           .appendText(" with value ").appendDescriptionOf(valueMatcher);
	}
}
