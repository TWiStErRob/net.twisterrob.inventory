package net.twisterrob.android.test.matchers;

import java.lang.reflect.*;

import org.hamcrest.*;

import static org.hamcrest.Condition.*;
import static org.hamcrest.beans.PropertyUtil.*;

import net.twisterrob.java.utils.ReflectionTools;

public class HasPropertyWithValueLite<T> extends TypeSafeDiagnosingMatcher<T> {
	private static final Condition.Step<Method, Method> WITH_READ_METHOD = withReadMethod();
	private final String propertyName;
	private final Matcher<Object> valueMatcher;

	public HasPropertyWithValueLite(String propertyName, Matcher<?> valueMatcher) {
		this.propertyName = propertyName;
		this.valueMatcher = nastyGenericsWorkaround(valueMatcher);
	}

	@Override
	public boolean matchesSafely(T bean, Description mismatch) {
		return propertyOn(bean, mismatch)
				.and(WITH_READ_METHOD)
				.and(withPropertyValue(bean))
				.matching(valueMatcher, "property '" + propertyName + "' ");
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("hasProperty(").appendValue(propertyName).appendText(", ")
		           .appendDescriptionOf(valueMatcher).appendText(")");
	}

	private Condition<Method> propertyOn(T bean, Description mismatch) {
		Method property = ReflectionTools.tryFindDeclaredMethod(bean.getClass(), getGetMethodName(propertyName));
		if (property == null) {
			property = ReflectionTools.tryFindDeclaredMethod(bean.getClass(), getIsMethodName(propertyName));
		}
		if (property == null) {
			mismatch.appendText("No property \"" + propertyName + "\"");
			return notMatched();
		}

		return matched(property, mismatch);
	}

	private Condition.Step<Method, Object> withPropertyValue(final T bean) {
		return new Condition.Step<Method, Object>() {
			@Override
			public Condition<Object> apply(Method readMethod, Description mismatch) {
				try {
					return matched(readMethod.invoke(bean, NO_ARGUMENTS), mismatch);
				} catch (Exception e) {
					mismatch.appendText(e.getMessage());
					return notMatched();
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	private static Matcher<Object> nastyGenericsWorkaround(Matcher<?> valueMatcher) {
		return (Matcher<Object>)valueMatcher;
	}

	private static String getGetMethodName(String propertyName) {
		return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
	}
	private static String getIsMethodName(String propertyName) {
		return "is" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
	}

	private static Condition.Step<Method, Method> withReadMethod() {
		return new Condition.Step<Method, Method>() {
			@Override
			public Condition<Method> apply(Method readMethod, Description mismatch) {
				if (!Modifier.isPublic(readMethod.getModifiers())) {
					mismatch.appendText("property read method \"" + readMethod.getName() + "\" is not public");
					return notMatched();
				}
				if (Modifier.isStatic(readMethod.getModifiers())) {
					mismatch.appendText("property read method \"" + readMethod.getName() + "\" is static");
					return notMatched();
				}
				return matched(readMethod, mismatch);
			}
		};
	}

	public static <T> Matcher<T> hasProperty(String propertyName, Matcher<?> valueMatcher) {
		return new HasPropertyWithValueLite<>(propertyName, valueMatcher);
	}
}
