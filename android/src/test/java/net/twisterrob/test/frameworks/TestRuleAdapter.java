package net.twisterrob.test.frameworks;

import org.junit.rules.*;
import org.junit.runner.Description;
import org.junit.runners.model.*;

import androidx.annotation.NonNull;

/**
 * @see <a href="https://github.com/powermock/powermock/issues/396#issuecomment-124665686">based on</a>
 * @see MethodRuleAdapter
 */
public class TestRuleAdapter implements MethodRule {

	private final @NonNull TestRule wrappedRule;

	public TestRuleAdapter(@NonNull TestRule wrappedRule) {
		this.wrappedRule = wrappedRule;
	}

	@Override public Statement apply(Statement base, FrameworkMethod method, Object target) {
		Description description = Description.createTestDescription(target.getClass(), method.getName());
		return wrappedRule.apply(base, description);
	}
}
