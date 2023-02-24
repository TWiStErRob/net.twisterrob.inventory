package net.twisterrob.inventory.android.test.actors;

import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.recyclerview.RecyclerViewDataInteraction;
import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class GridBelongingActor {
	private final String name;
	public GridBelongingActor(String name) {
		this.name = name;
	}

	public void assertHasImage() {
		interact().onChildView(withId(R.id.image)).check(matches(hasImage()));
	}
	private RecyclerViewDataInteraction interact() {
		return onRecyclerItem(withText(name));
	}
	public ItemViewActivityActor openAsItem() {
		interact().perform(click());
		ItemViewActivityActor actor = new ItemViewActivityActor();
		actor.assertIsInFront();
		return actor;
	}
	public void assertHasTypeImage() {
		interact().onChildView(withId(R.id.type)).check(matches(hasImage()));
	}
	public ChangeTypeDialogActor changeCategory() {
		interact().onChildView(withId(R.id.type)).perform(click());
		ChangeTypeDialogActor actor = new ChangeTypeDialogActor();
		actor.assertOpen();
		return actor;
	}
}
