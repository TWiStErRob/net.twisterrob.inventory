package net.twisterrob.android.test.espresso;

import org.hamcrest.Matcher;

import android.os.Debug;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.test.espresso.FailureHandler;

public class Ignore implements FailureHandler {
	private final @Nullable Boolean silent;

	public Ignore() {
		this.silent = null;
	}

	public Ignore(boolean silent) {
		this.silent = silent;
	}

	@Override public void handle(Throwable error, Matcher<View> viewMatcher) {
		boolean ignoreSilently = silent != null? silent : !Debug.isDebuggerConnected();
		if (!ignoreSilently) {
			log(error, viewMatcher);
		}
	}

	protected void log(Throwable error, Matcher<View> viewMatcher) {
		Log.wtf("Ignore", "Ignored error " + viewMatcher, error);
	}
}
