package net.twisterrob.inventory.android.test.actors;

import androidx.annotation.StringRes;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.PreferencesActivity;

import static net.twisterrob.android.test.espresso.EspressoExtensions.onRecyclerItem;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class PreferencesActivityActor extends ActivityActor {
	public PreferencesActivityActor() {
		super(PreferencesActivity.class);
	}

	public AboutActivityActor openAbout() {
		onRecyclerItem(withText(R.string.about_title)).perform(click());
		AboutActivityActor actor = new AboutActivityActor();
		actor.assertIsInFront();
		return actor;
	}
	public void openAppInfoInSettings() {
		onRecyclerItem(withText(R.string.pref_app_in_settings_title)).perform(click());
	}
	public void openAppInfoInMarket() {
		onRecyclerItem(withText(R.string.pref_app_in_store_title)).perform(click());
	}
	public void setDetailsPage(@StringRes int detailsPage) {
		onRecyclerItem(withText(R.string.pref_defaultViewPage_title)).perform(click());
		onData(isString(detailsPage)).perform(click());
		onRecyclerItem(withText(R.string.pref_defaultViewPage_title))
				.onChildView(withId(android.R.id.summary))
				.check(matches(withText(detailsPage)));
	}
}
