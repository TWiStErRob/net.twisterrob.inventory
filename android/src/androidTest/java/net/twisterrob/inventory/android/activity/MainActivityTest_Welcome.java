package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.view.View;

import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.actors.MainActivityActor.WelcomeDialogActor;
import net.twisterrob.inventory.android.test.categories.*;
import net.twisterrob.test.junit.FlakyTestException;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Main.class})
public class MainActivityTest_Welcome {

	private static final ViewAssertion NON_EMPTY =
			matches(allOf(isCompletelyDisplayed(), hasDescendant(notNullValue(View.class))));
	private static final ViewAssertion EMPTY =
			matches(allOf(isCompletelyDisplayed(), not(hasDescendant(notNullValue(View.class)))));

	@SuppressWarnings("deprecation")
	@Rule public final androidx.test.rule.ActivityTestRule<MainActivity> activity =
			new InventoryActivityRule<>(MainActivity.class)
					.dontClearWelcomeFlag();

	private final MainActivityActor main = new MainActivityActor();

	@Category({UseCase.InitialCondition.class, Op.Cancels.class})
	@Test public void testWelcomeShownAndClosable() {
		WelcomeDialogActor welcome = main.assertWelcomeShown();

		// Dismiss the dialog to not leak the window.
		welcome.dismissIgnoring();
		main.assertClosing();
	}

	@Category({Op.Cancels.class, Op.ChecksMessage.class})
	@Test public void testWelcomeDontPopulateDemo() {
		WelcomeDialogActor welcome = main.assertWelcomeShown();

		onView(isRoot()).perform(waitForToastsToDisappear());
		long beforePotentiallyLongAction = System.currentTimeMillis();
		welcome.dontPopulateDemo();

		checkToasted(beforePotentiallyLongAction);
		onView(withId(R.id.properties)).check(EMPTY);
		onView(withId(R.id.rooms)).check(EMPTY);
		onView(withId(R.id.lists)).check(EMPTY);
		onView(withId(R.id.items)).check(EMPTY);
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testWelcomePopulateDemo() {
		WelcomeDialogActor welcome = main.assertWelcomeShown();

		onView(isRoot()).perform(waitForToastsToDisappear());
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

		onView(isRoot()).perform(waitForToastsToDisappear());
		BackupActivityActor backup = welcome.invokeBackup();
		assertNoToastIsDisplayed();

		backup.openedViaIntent();
	}

	private void checkToasted(long start) {
		try {
			onView(isRoot()).inRoot(isToast()).check(matches(isCompletelyDisplayed()));
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
