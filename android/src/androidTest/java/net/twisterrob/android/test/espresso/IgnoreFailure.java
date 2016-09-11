package net.twisterrob.android.test.espresso;

import org.hamcrest.Matcher;

import android.support.test.espresso.FailureHandler;
import android.view.View;

public class IgnoreFailure implements FailureHandler {
	@Override public void handle(Throwable error, Matcher<View> viewMatcher) {
		// ignore
	}
}
