package net.twisterrob.inventory.android.test.actors;

import androidx.annotation.StringRes;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.PreferencesActivity;

import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class PreferencesActivityActor extends ActivityActor {
	public PreferencesActivityActor() {
		super(PreferencesActivity.class);
	}

	public AboutActivityActor openAbout() {
		onData(withTitle(isText(R.string.about_title))).perform(click());
		AboutActivityActor actor = new AboutActivityActor();
		actor.assertIsInFront();
		return actor;
	}
	public void openAppInfoInSettings() {
		onData(withTitle(isText(R.string.pref_app_in_settings_title))).perform(click());
	}
	public void openAppInfoInMarket() {
		onData(withTitle(isText(R.string.pref_app_in_store_title))).perform(click());
	}
	public void setDetailsPage(@StringRes int detailsPage) {
		onData(withKey(isString(R.string.pref_defaultViewPage))).perform(click());
		onData(isString(detailsPage)).perform(click());
		onData(withKey(isString(R.string.pref_defaultViewPage)))
				.onChildView(withId(android.R.id.summary))
				.check(matches(withText(detailsPage)));
	}
}
