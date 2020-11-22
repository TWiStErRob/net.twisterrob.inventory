package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

import android.app.Activity;

import androidx.test.runner.lifecycle.Stage;

import static androidx.test.espresso.Espresso.*;
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
