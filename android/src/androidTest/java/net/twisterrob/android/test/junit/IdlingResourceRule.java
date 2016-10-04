package net.twisterrob.android.test.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.*;

import android.support.annotation.NonNull;
import android.support.test.espresso.*;

public class IdlingResourceRule implements TestRule {
	private static final Logger LOG = LoggerFactory.getLogger(IdlingResourceRule.class);

	private final IdlingResource idlingResource;
	private boolean verbose = false;

	public IdlingResourceRule(@NonNull IdlingResource resource) {
		idlingResource = resource;
	}

	public IdlingResourceRule beVerbose() {
		verbose = true;
		return this;
	}

	public @NonNull IdlingResource getIdlingResource() {
		return idlingResource;
	}

	@Override public Statement apply(final Statement base, Description description) {
		return new IdlingResourceStatement(base);
	}

	private class IdlingResourceStatement extends Statement {
		private final Statement base;
		public IdlingResourceStatement(Statement base) {
			this.base = base;
		}
		@Override public void evaluate() throws Throwable {
			try {
				register(getIdlingResource());
				base.evaluate();
			} finally {
				unregister(getIdlingResource());
			}
		}
	}

	protected void register(IdlingResource resource) {
		if (verbose) {
			LOG.trace("Registering {}", resource);
		}
		Espresso.registerIdlingResources(resource);
	}

	protected void unregister(IdlingResource resource) {
		if (verbose) {
			LOG.trace("Unregistering {}", resource);
		}
		Espresso.unregisterIdlingResources(resource);
	}
}
