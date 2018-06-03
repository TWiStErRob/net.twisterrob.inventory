package net.twisterrob.android.test.espresso;

import org.hamcrest.Matcher;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.*;
import android.support.test.espresso.base.DefaultFailureHandler;
import android.view.View;

/**
 * @deprecated looks like {@code .withFailureHandler(new PassMissingRoot())}
 * doesn't work the same in Espresso 3.x as in 2.x.
 * In 3.x there's a 60 second wait before NoMatchingRootException is thrown, resulting in really long tests.
 * When checking for missing root, use for example: {@code assertFalse(hasRoot(<someRootMatcher()>))}.
 */
@Deprecated
public class PassMissingRoot implements FailureHandler {
	private final FailureHandler defaultHandler =
			new DefaultFailureHandler(InstrumentationRegistry.getTargetContext());
	@Override public void handle(Throwable error, Matcher<View> viewMatcher) {
		if (!(error instanceof NoMatchingRootException)) {
			defaultHandler.handle(error, viewMatcher);
		}
	}
}
