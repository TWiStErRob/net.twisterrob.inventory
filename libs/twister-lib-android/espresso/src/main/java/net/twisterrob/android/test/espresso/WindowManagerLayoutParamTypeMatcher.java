package net.twisterrob.android.test.espresso;

import org.hamcrest.*;

import android.os.IBinder;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.NonNull;
import androidx.test.espresso.Root;

public class WindowManagerLayoutParamTypeMatcher extends TypeSafeMatcher<Root> {

	private final @NonNull String description;

	/**
	 * Note: If it was a window for an activity, it would have {@link LayoutParams#TYPE_BASE_APPLICATION}.
	 */
	private final int expectedType;

	private final boolean expectedWindowTokenMatch;

	/**
	 * @param description "is [foo]"-like text to describe the type being matched
	 * @param expectedType One of {@code TYPE_*} from {@link LayoutParams}
	 * @see LayoutParams#type
	 */
	public WindowManagerLayoutParamTypeMatcher(
			@NonNull String description, int expectedType) {
		this(description, expectedType, true);
	}

	/**
	 * @param description "is [foo]"-like text to describe the type being matched
	 * @param expectedType One of {@code TYPE_*} from {@link LayoutParams}
	 * @param expectedWindowTokenMatch whether to expect window token to match the application,
	 *                                 that is to check whether the window belongs to the application.
	 * @see LayoutParams#type
	 */
	public WindowManagerLayoutParamTypeMatcher(
			@NonNull String description, int expectedType, boolean expectedWindowTokenMatch) {
		this.description = description;
		this.expectedType = expectedType;
		this.expectedWindowTokenMatch = expectedWindowTokenMatch;
	}

	@Override public void describeTo(Description description) {
		description.appendText(this.description);
	}

	@Override public boolean matchesSafely(Root root) {
		return expectedType == root.getWindowLayoutParams().get().type
				&& expectedWindowTokenMatch == tokensMatch(root.getDecorView());
	}

	/**
	 * @param decorView the {@link View} whose {@link android.view.Window} to interrogate.
	 * @return {@code true}, the view's window isn't contained by any other windows
	 */
	private boolean tokensMatch(@NonNull View decorView) {
		IBinder windowToken = decorView.getWindowToken();
		IBinder appToken = decorView.getApplicationWindowToken();
		return windowToken == appToken;
	}
}
