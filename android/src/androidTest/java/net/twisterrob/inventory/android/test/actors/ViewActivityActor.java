package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.action.ViewActions;
import androidx.test.runner.lifecycle.Stage;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.SingleFragmentActivity;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.junit.InstrumentationExtensions.*;

public abstract class ViewActivityActor extends ActivityActor {
	public ViewActivityActor(Class<? extends Activity> activityClass) {
		super(activityClass);
	}
	public abstract void assertShowing(String name);

	public void assertImageVisible() {
		onView(withId(R.id.image))
				.check(matches(isCompletelyDisplayed()))
				.check(matches(hasImage()))
		;
	}
	public void assertTypeVisible() {
		onView(withId(R.id.type))
				.check(matches(isCompletelyDisplayed()))
				.check(matches(hasImage()))
		;
	}
	public void assertDetailsVisible() {
		onView(withId(R.id.details))
				.check(matches(isCompletelyDisplayed()))
		;
	}
	public void assertDetailsText(Matcher<String> textMatcher) {
		onView(withId(R.id.details))
				.check(matches(isCompletelyDisplayed()))
				.check(matches(withText(textMatcher)))
		;
	}

	protected void clickFab() {
		// Swipe the list to ensure the FAB is visible.
		// See net.twisterrob.inventory.android.view.RecyclerViewController.lastScrollUp.
		onView(withId(android.R.id.list))
				// Swipe from middle, because the header is not swipeable vertically, only sideways.
				.perform(swipeDownFromMiddle());
		onView(withId(R.id.fab))
				.perform(click());
	}

	/**
	 * @see ViewActions#swipeDown()
	 */
	private static @NonNull ViewAction swipeDownFromMiddle() {
		return ViewActions.actionWithAssertions(
				new GeneralSwipeAction(
						Swipe.FAST,
						GeneralLocation.CENTER,
						GeneralLocation.BOTTOM_CENTER,
						Press.FINGER
				)
		);
	}

	/** @deprecated should use a better {@link net.twisterrob.android.test.junit.SensibleActivityTestRule}. */
	@Deprecated
	public void refresh() {
		runOnMainIfNecessary(new Runnable() {
			@Override public void run() {
				Activity activity = getActivityInStage(Stage.RESUMED);
				assertThat(activity, instanceOf(SingleFragmentActivity.class));
				((SingleFragmentActivity<?>)activity).getFragment().refresh();
			}
		});
	}
}
