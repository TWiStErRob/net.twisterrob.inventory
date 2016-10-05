package net.twisterrob.inventory.android.test.actors;

import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.EspressoExtensions;
import net.twisterrob.inventory.android.activity.SearchResultsActivity;

public class SearchResultActivityActor extends ActivityActor {
	public SearchResultActivityActor() {
		super(SearchResultsActivity.class);
	}
	public SearchActor openSearch() {
		SearchActor actor = new SearchActor();
		actor.open();
		return actor;
	}
	public void assertResultShown(String item) {
		EspressoExtensions.onRecyclerItem(withText(item)).check(matches(isDisplayed()));
	}
	public ItemViewActivityActor openResult(String item) {
		EspressoExtensions.onRecyclerItem(withText(item)).perform(click());
		ItemViewActivityActor actor = new ItemViewActivityActor();
		actor.assertShowing(item);
		return actor;
	}
}
