package net.twisterrob.android.test.espresso;

import java.util.concurrent.TimeoutException;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.*;
import android.support.test.espresso.NoMatchingViewException.Builder;
import android.support.test.espresso.util.*;
import android.support.test.rule.ActivityTestRule;
import android.view.View;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.recyclerview.RecyclerViewDataInteraction;

public class EspressoExtensions {
	/**
	 * Perform action of waiting for a specific view id.
	 * @see <a href="http://stackoverflow.com/a/22563297/253468">Espresso: Thread.sleep( );</a>
	 */
	public static ViewAction waitFor(final Matcher<View> viewMatcher, final long millis) {
		return new ViewAction() {
			@Override public Matcher<View> getConstraints() {
				return isRoot();
			}

			@Override public String getDescription() {
				return "wait for " + StringDescription.asString(viewMatcher) + " for " + millis + " milliseconds.";
			}

			@Override public void perform(final UiController uiController, final View view) {
				uiController.loopMainThreadUntilIdle();
				final long startTime = System.currentTimeMillis();
				final long endTime = startTime + millis;

				long step = Math.max(1, Math.min(50, millis / 10)); // 10th of input between 1..50ms 
				do {
					for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
						if (viewMatcher.matches(child)) {
							return;
						}
					}
					uiController.loopMainThreadForAtLeast(step);
					//uiController.loopMainThreadUntilIdle();
				} while (System.currentTimeMillis() < endTime);

				TimeoutException timeout = new TimeoutException();
				NoMatchingViewException noMatchException = new Builder()
						.withRootView(view)
						.withViewMatcher(viewMatcher)
						.build();
				timeout.initCause(noMatchException);
				throw new PerformException.Builder()
						.withActionDescription(this.getDescription())
						.withViewDescription(HumanReadables.describe(view))
						.withCause(timeout)
						.build();
			}
		};
	}

	public static void rotateDevice(ActivityTestRule<?> activity) {
		activity.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getInstrumentation().waitForIdleSync();
	}

	public static Matcher<Intent> chooser(Matcher<Intent> matcher) {
		return allOf(hasAction(Intent.ACTION_CHOOSER), hasExtra(is(Intent.EXTRA_INTENT), matcher));
	}

	/** @see Espresso#onData(Matcher) */
	public static RecyclerViewDataInteraction onRecyclerItem(Matcher<View> dataMatcher) {
		return new RecyclerViewDataInteraction(hasDescendant(dataMatcher));
	}

	/** @see Espresso#onData(Matcher) */
	public static RecyclerViewDataInteraction onRecyclerItemExact(Matcher<View> dataMatcher) {
		return new RecyclerViewDataInteraction(dataMatcher);
	}
}
