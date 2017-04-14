package net.twisterrob.test.frameworks;


import java.lang.reflect.Method;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * @see <a href="https://github.com/powermock/powermock/issues/396#issuecomment-124665686">based on</a>
 */
public class MethodRuleAdapter implements TestRule {

	private final MethodRule wrappedRule;

	public MethodRuleAdapter(MethodRule wrappedRule) {
		this.wrappedRule = wrappedRule;
	}

	@Override public Statement apply(Statement base, Description testDescription) {
		return wrappedRule.apply(base, createFrameworkMethod(testDescription), getTestObject(testDescription));
	}

	private FrameworkMethod createFrameworkMethod(Description testDescription) {
		try {
			String testMethodName = testDescription.getMethodName();
			Method testMethod = testDescription.getTestClass().getDeclaredMethod(testMethodName);
			return new FrameworkMethod(testMethod);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private Object getTestObject(Description testDescription) {
		try {
			return testDescription.getTestClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
}
