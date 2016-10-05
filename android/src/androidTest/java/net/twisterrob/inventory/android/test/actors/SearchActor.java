package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

import android.annotation.SuppressLint;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.RootMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class SearchActor {
	public void open() {
		onView(withId(R.id.search)).perform(click());
		assertOpened();
	}
	@SuppressLint("PrivateResource")
	public void close() {
		onView(withContentDescription(R.string.abc_toolbar_collapse_description)).perform(click());
		assertClosed();
	}
	public void setQuery(String query) {
		assertOpened();
		onView(withId(R.id.search_src_text)).perform(replaceText(query));
		assertQuery(is(query));
	}
	public SearchResultActivityActor search() {
		assertOpened();
		onView(withId(R.id.search_src_text)).perform(pressImeActionButton());
		SearchResultActivityActor actor = new SearchResultActivityActor();
		actor.assertIsInFront();
		return actor;
	}
	public void clear() {
		assertOpened();
		onView(withId(R.id.search_close_btn)).perform(click());
		assertQuery(is(emptyString()));
	}
	public void assertQuery(Matcher<String> stringMatcher) {
		assertOpened();
		onView(withId(R.id.search_src_text)).check(matches(withText(stringMatcher)));
	}
	public void assertOpened() {
		onView(isSearchView()).check(matches(isDisplayed()));
	}
	public void assertClosed() {
		onView(isSearchView()).check(doesNotExist());
	}

	public ItemViewActivityActor select(Matcher<String> matcher) {
		onData(searchSuggestion(matcher))
				.inRoot(isPlatformPopup())
				.perform(click());
		return new ItemViewActivityActor();
	}
	public void assertResultShown(Matcher<String> matcher) {
		onData(searchSuggestion(matcher))
				.inRoot(isPlatformPopup())
				.check(matches(isDisplayed()))
		;
	}
}
