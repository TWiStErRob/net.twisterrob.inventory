package net.twisterrob.inventory.android.test;

import org.junit.rules.ExternalResource;

import androidx.test.espresso.IdlingRegistry;

import net.twisterrob.android.test.espresso.idle.GlideIdlingResource;

class GlideIdlingResourceRule extends ExternalResource {
	private final GlideIdlingResource glideIdler = new GlideIdlingResource();

	@Override protected void before() {
		IdlingRegistry.getInstance().register(glideIdler);
	}

	@Override protected void after() {
		IdlingRegistry.getInstance().unregister(glideIdler);
	}
}
