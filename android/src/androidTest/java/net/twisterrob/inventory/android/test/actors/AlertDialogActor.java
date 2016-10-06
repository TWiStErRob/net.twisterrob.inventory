package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;

public class AlertDialogActor {
	protected final void assertDialogMessage(Matcher<String> matcher) {
		onView(withText(matchesPattern("%\\d"))).check(doesNotExist());
		onView(isDialogMessage()).check(matches(allOf(
				isCompletelyDisplayed(),
				withText(matcher)
		)));
	}
	protected final void assertToastMessage(Matcher<String> matcher) {
		onView(isDialogMessage()).inRoot(isToast()).check(matches(allOf(
				isCompletelyDisplayed(),
				withText(matcher)
		)));
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

