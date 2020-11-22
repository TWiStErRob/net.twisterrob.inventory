package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;

public class AlertDialogActor {
	protected final void assertDialogMessage(Matcher<String> matcher) {
		onView(withText(matchesPattern("%\\d"))).check(doesNotExist());
		onView(isDialogMessage())
				.check(matches(isCompletelyDisplayed()))
				.check(matches(withText(matcher)))
		;
	}
	protected final void assertToastMessage(Matcher<String> matcher) {
		onView(isDialogMessage())
				.inRoot(isToast())
				.check(matches(isCompletelyDisplayed()))
				.check(matches(withText(matcher)))
		;
	}
	public void assertNoToastDisplayed() {
		assertNoToastIsDisplayed();
	}
	public void assertNotDisplayed() {
		assertNoDialogIsDisplayed();
	}
	public void assertDisplayed() {
		assertDialogIsDisplayed();
	}
	protected void dismissWitNeutral() {
		clickNeutralInDialog();
		assertNoDialogIsDisplayed();
	}
}

