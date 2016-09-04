package net.twisterrob.android.test.espresso;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.support.annotation.IdRes;
import android.support.test.espresso.*;
import android.support.test.espresso.util.*;
import android.view.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.RootMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

public class DialogMatchers {
	public static final int BUTTON_POSITIVE = android.R.id.button1;
	public static final int BUTTON_NEGATIVE = android.R.id.button2;
	public static final int BUTTON_NEUTRAL = android.R.id.button3;

	public static Matcher<View> root(final Matcher<Root> matcher) {
		return new TypeSafeMatcher<View>() {
			@Override protected boolean matchesSafely(View view) {
				boolean isRoot = isRoot().matches(view);
				if (isRoot) {
					Root root = new Root.Builder()
							.withDecorView(view)
							.withWindowLayoutParams((WindowManager.LayoutParams)view.getLayoutParams())
							.build();
					return matcher.matches(root);
				}
				return false;
			}
			@Override public void describeTo(Description description) {
				description.appendText("is a root view and matches ").appendDescriptionOf(matcher);
			}
		};
	}

	public static Matcher<View> isDialogView() {
		return root(isDialog());
	}

	private static void clickInDialog(@IdRes int buttonId) {
		onView(withId(buttonId)).inRoot(isDialog()).perform(click());
	}
	public static void clickPositiveInDialog() {
		clickInDialog(BUTTON_POSITIVE);
	}
	public static void clickNegativeInDialog() {
		clickInDialog(BUTTON_NEGATIVE);
	}
	public static void clickNeutralInDialog() {
		clickInDialog(BUTTON_NEUTRAL);
	}

	/**
	 * <pre><code>onView(isDialogView()).perform(clickPositive());</code></pre>
	 * @see #isDialogView()
	 */
	public static ViewAction clickPositive() {
		return new ClickInDialog(withId(BUTTON_POSITIVE));
	}
	/**
	 * <pre><code>onView(isDialogView()).perform(clickNegative());</code></pre>
	 * @see #isDialogView()
	 */
	public static ViewAction clickNegative() {
		return new ClickInDialog(withId(BUTTON_NEGATIVE));
	}
	/**
	 * <pre><code>onView(isDialogView()).perform(clickNeutral());</code></pre>
	 * @see #isDialogView()
	 */
	public static ViewAction clickNeutral() {
		return new ClickInDialog(withId(BUTTON_NEUTRAL));
	}

	public static void assertDialogIsDisplayed() {
		// both of the below statements should be equivalent
		onView(isRoot()).inRoot(isDialog()).check(matches(isDisplayed()));
		onView(isRoot()).check(matches(root(isDialog())));
	}

	public static void assertNoDialogIsDisplayed() {
		// required to use the root(...) option because these fail too early on inRoot(isDialog())
		//onView(isRoot()).inRoot(isDialog()).check(doesNotExist());
		//onView(isRoot()).inRoot(isDialog()).check(matches(not(isDisplayed())));
		// this works but the other is more concise
		//onView(isRoot()).check(matches(root(not(RootMatchers.isDialog()))));
		onView(isDialogView()).check(doesNotExist());
	}

	private static class ClickInDialog implements ViewAction {
		private final Matcher<View> viewMatcher;
		public ClickInDialog(Matcher<View> viewMatcher) {
			this.viewMatcher = viewMatcher;
		}
		@Override public Matcher<View> getConstraints() {
			return allOf(isDisplayed(), root(isDialog()));
		}
		@Override public String getDescription() {
			return "Click " + StringDescription.asString(viewMatcher) + " in a dialog.";
		}
		@Override public void perform(UiController uiController, View view) {
			Matcher<View> dialogSpecificMatcher = allOf(viewMatcher, isDescendantOfA(is(view)));
			for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
				if (dialogSpecificMatcher.matches(child)) {
					ViewAction click = click();
					if (!click.getConstraints().matches(child)) {
						throw new PerformException.Builder()
								.withActionDescription(click.getDescription())
								.withViewDescription(HumanReadables.describe(child))
								.build();
					}
					click.perform(uiController, child);
					return;
				}
			}
			throw new PerformException.Builder()
					.withActionDescription(this.getDescription())
					.withViewDescription(HumanReadables.describe(view))
					.withCause(new NoMatchingViewException.Builder()
							.withRootView(view)
							.withViewMatcher(dialogSpecificMatcher)
							.build()
					)
					.build();
		}
	}
}
