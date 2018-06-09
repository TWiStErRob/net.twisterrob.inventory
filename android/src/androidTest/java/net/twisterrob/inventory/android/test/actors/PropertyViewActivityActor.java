package net.twisterrob.inventory.android.test.actors;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.PropertyViewActivity;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class PropertyViewActivityActor extends ViewActivityActor {
	public PropertyViewActivityActor() {
		super(PropertyViewActivity.class);
	}
	@Override public void assertShowing(String propertyName) {
		assertActionTitle(propertyName);
	}
	public DeleteDialogActor delete() {
		clickActionOverflow(R.id.action_property_delete);
		return new DeleteDialogActor();
	}
	public void hasNoRoom(String roomName) {
		onView(withId(android.R.id.list)).check(itemDoesNotExists(withText(roomName)));
	}
	public void hasRoom(String roomName) {
		onRecyclerItem(withText(roomName)).check(matches(isCompletelyDisplayed()));
	}
}
