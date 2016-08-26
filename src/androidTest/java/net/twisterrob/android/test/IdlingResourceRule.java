package net.twisterrob.android.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.support.annotation.NonNull;
import android.support.test.espresso.*;

public class IdlingResourceRule implements TestRule {
	private IdlingResource idlingResource;

	public IdlingResourceRule(@NonNull IdlingResource resource) {
		idlingResource = resource;
	}

	public @NonNull IdlingResource getIdlingResource() {
		return idlingResource;
	}

	@Override public Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override public void evaluate() throws Throwable {
				try {
					Espresso.registerIdlingResources(idlingResource);
					base.evaluate();
				} finally {
					Espresso.unregisterIdlingResources(idlingResource);
				}
			}
		};
	}
}
