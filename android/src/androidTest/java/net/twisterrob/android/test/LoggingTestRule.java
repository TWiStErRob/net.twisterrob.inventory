package net.twisterrob.android.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.*;

public class LoggingTestRule implements TestRule {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingTestRule.class);
	private final String name;

	public LoggingTestRule() {
		this(null);
	}
	public LoggingTestRule(String name) {
		this.name = name != null? name + "." : "";
	}

	@Override public Statement apply(final Statement base, Description description) {
		LOG.trace("{}apply({}, {})", name, base, description);
		return new Statement() {
			@Override public void evaluate() throws Throwable {
				try {
					LOG.trace("{}evaluate({})", name, base);
					base.evaluate();
				} finally {
					LOG.trace("{}evaluated({})", name, base);
				}
			}
		};
	}
}
