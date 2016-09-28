package net.twisterrob.android.test.espresso;

import org.hamcrest.Matcher;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.*;
import android.support.test.espresso.base.DefaultFailureHandler;
import android.view.View;

public class PassMissingRoot implements FailureHandler {
	private final FailureHandler defaultHandler =
			new DefaultFailureHandler(InstrumentationRegistry.getTargetContext());
	@Override public void handle(Throwable error, Matcher<View> viewMatcher) {
		if (!(error instanceof NoMatchingRootException)) {
			defaultHandler.handle(error, viewMatcher);
		}
	}
}
