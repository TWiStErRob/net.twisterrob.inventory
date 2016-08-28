package net.twisterrob.inventory.android.activity;

import static org.hamcrest.Matchers.*;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;

public class BackupActivityTest {
	static void assertEmptyState() {
		// no result is picked up from saved state or notification intent
		assertNoDialogIsDisplayed();
		// progress bar is not displayed
		onView(withId(R.id.progress)).check(matches(not(isDisplayed())));
	}
}
