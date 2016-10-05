package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;

import android.app.Activity;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public abstract class ViewActivityActor extends ActivityActor {
	public ViewActivityActor(Class<? extends Activity> activityClass) {
		super(activityClass);
	}
	public abstract void assertShowing(String name);

	public void assertImageVisible() {
		onView(withId(R.id.image))
				.check(matches(isDisplayed()))
				.check(matches(hasImage()))
		;
	}
	public void assertTypeVisible() {
		onView(withId(R.id.type))
				.check(matches(isDisplayed()))
				.check(matches(hasImage()))
		;
	}
	public void assertDetailsVisible() {
		onView(withId(R.id.details))
				.check(matches(isDisplayed()))
		;
	}
	public void assertDetailsText(Matcher<String> textMatcher) {
		onView(withId(R.id.details))
				.check(matches(isDisplayed()))
				.check(matches(withText(textMatcher)))
		;
	}
}
