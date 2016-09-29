package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.support.test.espresso.ViewAssertion;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.actors.MainActivityActor.WelcomeDialogActor;
import net.twisterrob.test.junit.FlakyTestException;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest_Welcome {

	private static final ViewAssertion NON_EMPTY =
			matches(allOf(isDisplayed(), hasDescendant(notNullValue(View.class))));
	private static final ViewAssertion EMPTY =
			matches(allOf(isDisplayed(), not(hasDescendant(notNullValue(View.class)))));

	@Rule public ActivityTestRule<MainActivity> activity = new InventoryActivityRule<>(MainActivity.class)
			.dontClearWelcomeFlag();
	private final MainActivityActor main = new MainActivityActor();

	@Test public void testWelcomeDontPopulateDemo() {
		WelcomeDialogActor welcome = main.assertWelcomeShown();

		long beforePotentiallyLongAction = System.currentTimeMillis();
		welcome.dontPopulateDemo();

		checkToasted(beforePotentiallyLongAction);
		onView(withId(R.id.properties)).check(EMPTY);
		onView(withId(R.id.rooms)).check(EMPTY);
		onView(withId(R.id.lists)).check(EMPTY);
		onView(withId(R.id.items)).check(EMPTY);
	}

	@Test public void testWelcomePopulateDemo() {
		WelcomeDialogActor welcome = main.assertWelcomeShown();

		long beforePotentiallyLongAction = System.currentTimeMillis();
		welcome.populateDemo();

		checkToasted(beforePotentiallyLongAction);
		onView(withId(R.id.properties)).check(NON_EMPTY);
		onView(withId(R.id.rooms)).check(NON_EMPTY);
		onView(withId(R.id.lists)).check(NON_EMPTY);
		onView(withId(R.id.items)).check(EMPTY);
	}

	@Test public void testWelcomeBackup() {
		WelcomeDialogActor welcome = main.assertWelcomeShown();

		BackupActivityActor backup = welcome.invokeBackup();

		backup.openedViaIntent();
	}

	private void checkToasted(long start) {
		try {
			onView(isRoot()).inRoot(isToast()).check(matches(isDisplayed()));
		} catch (RuntimeException ex) {
			long checkDuration = System.currentTimeMillis() - start;
			if (2000 < checkDuration) { // NotificationManagerService.SHORT_DELAY = 2000
				// TODO is there a way to ignore some idle resources (AsyncTasks in this case) and go ahead with check?
				throw new FlakyTestException("Idle resources took too long, the toast probably disappeared.", ex);
			} else {
				throw ex;
			}
		}
	}
}
