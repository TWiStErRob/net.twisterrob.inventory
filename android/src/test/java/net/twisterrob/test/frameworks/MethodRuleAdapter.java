package net.twisterrob.test.frameworks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.rules.*;
import org.junit.runner.Description;
import org.junit.runners.model.*;

import androidx.annotation.NonNull;

/**
 * To use this, is a bad idea as it doesn't have a reference to the real deal, use with care.
 * @see <a href="https://github.com/powermock/powermock/issues/396#issuecomment-124665686">based on</a>
 * @see TestRuleAdapter
 */
public class MethodRuleAdapter implements TestRule {

	private final @NonNull MethodRule wrappedRule;

	public MethodRuleAdapter(@NonNull MethodRule wrappedRule) {
		this.wrappedRule = wrappedRule;
	}

	@Override public Statement apply(Statement base, Description testDescription) {
		FrameworkMethod method = createFrameworkMethod(testDescription);
		Object object = getTestObject(testDescription); // WARNING: this is a new object, not the real thing
		return wrappedRule.apply(base, method, object);
	}

	private @NonNull FrameworkMethod createFrameworkMethod(@NonNull Description testDescription) {
		try {
			String testMethodName = testDescription.getMethodName();
			Method testMethod = testDescription.getTestClass().getDeclaredMethod(testMethodName);
			return new FrameworkMethod(testMethod);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private @NonNull Object getTestObject(@NonNull Description testDescription) {
		try {
			return testDescription.getTestClass().getDeclaredConstructor().newInstance();
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException ex) {
			throw new IllegalStateException(ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}
}
