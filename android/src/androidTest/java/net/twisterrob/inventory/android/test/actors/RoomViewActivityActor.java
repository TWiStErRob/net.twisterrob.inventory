package net.twisterrob.inventory.android.test.actors;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.RoomViewActivity;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

public class RoomViewActivityActor extends ViewActivityActor {
	public RoomViewActivityActor() {
		super(RoomViewActivity.class);
	}
	@Override public void assertShowing(String roomName) {
		assertActionTitle(roomName);
	}

	public DeleteDialogActor delete() {
		clickActionOverflow(R.string.room_delete);
		return new DeleteDialogActor();
	}
	public MoveTargetActivityActor move() {
		clickActionOverflow(R.string.room_move);
		return new MoveTargetActivityActor();
	}
	public ItemEditActivityActor addItem() {
		onView(withId(R.id.fab)).perform(click());
		return new ItemEditActivityActor();
	}
	public ItemViewActivityActor openItem(String itemName) {
		onRecyclerItem(withText(itemName)).perform(click());
		return new ItemViewActivityActor();
	}
}
