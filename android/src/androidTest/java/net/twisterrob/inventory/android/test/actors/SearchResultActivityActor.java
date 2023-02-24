package net.twisterrob.inventory.android.test.actors;

import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

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
		EspressoExtensions.onRecyclerItem(withText(item)).check(matches(isCompletelyDisplayed()));
	}
	public ItemViewActivityActor openResult(String item) {
		EspressoExtensions.onRecyclerItem(withText(item)).perform(click());
		ItemViewActivityActor actor = new ItemViewActivityActor();
		actor.assertShowing(item);
		return actor;
	}
}
