package net.twisterrob.test.frameworks;

import java.util.*;

import org.junit.rules.MethodRule;
import org.junit.runners.model.*;

import android.support.annotation.NonNull;

/**
 * The {@link MethodRuleChain} rule allows ordering of {@link MethodRule}s.
 * @see org.junit.rules.RuleChain for more info.
 */
public class MethodRuleChain implements MethodRule {

	private static final MethodRuleChain EMPTY_CHAIN = new MethodRuleChain(Collections.<MethodRule>emptyList());

	private final @NonNull List<MethodRule> rulesStartingWithInnerMost;

	/**
	 * Returns a {@link MethodRuleChain} without a {@link MethodRule}.
	 * This method may be the starting point of a {@link MethodRuleChain}.
	 *
	 * @return a {@link MethodRuleChain} without a {@link MethodRule}.
	 */
	public static @NonNull MethodRuleChain emptyRuleChain() {
		return EMPTY_CHAIN;
	}

	/**
	 * Returns a {@link MethodRuleChain} with a single {@link MethodRule}.
	 * This method is the usual starting point of a {@link MethodRuleChain}.
	 *
	 * @param outerRule the outer rule of the {@link MethodRuleChain}.
	 * @return a {@link MethodRuleChain} with a single {@link MethodRule}.
	 */
	public static @NonNull MethodRuleChain outerRule(@NonNull MethodRule outerRule) {
		return emptyRuleChain().around(outerRule);
	}

	private MethodRuleChain(@NonNull List<MethodRule> rules) {
		this.rulesStartingWithInnerMost = rules;
	}

	/**
	 * Create a new {@link MethodRuleChain},
	 * which encloses the {@code enclosedRule} with the rules of the current {@link MethodRuleChain}.
	 *
	 * @param enclosedRule the rule to enclose.
	 * @return a new {@link MethodRuleChain}.
	 */
	public @NonNull MethodRuleChain around(@NonNull MethodRule enclosedRule) {
		List<MethodRule> rulesOfNewChain = new ArrayList<>();
		rulesOfNewChain.add(enclosedRule);
		rulesOfNewChain.addAll(rulesStartingWithInnerMost);
		return new MethodRuleChain(rulesOfNewChain);
	}

	@Override public Statement apply(Statement base, FrameworkMethod method, Object target) {
		for (MethodRule each : rulesStartingWithInnerMost) {
			base = each.apply(base, method, target);
		}
		return base;
	}
}
