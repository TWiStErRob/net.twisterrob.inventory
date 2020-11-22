package net.twisterrob.inventory.android.test.actors;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.RoomViewActivity;

public class RoomViewActivityActor extends ItemContainingViewActivityActor {
	public RoomViewActivityActor() {
		super(RoomViewActivity.class);
	}
	@Override public void assertShowing(String roomName) {
		assertActionTitle(roomName);
	}

	public DeleteDialogActor delete() {
		clickActionOverflow(R.id.action_room_delete);
		return new DeleteDialogActor();
	}
	public MoveTargetActivityActor move() {
		clickActionOverflow(R.id.action_room_move);
		return new MoveTargetActivityActor();
	}
	public ItemEditActivityActor addItem() {
		onView(withId(R.id.fab)).perform(click());
		return new ItemEditActivityActor();
	}
	public ItemViewActivityActor openItem(String itemName) {
		return item(itemName).openAsItem();
	}
	public GridBelongingActor item(String itemName) {
		return new GridBelongingActor(itemName);
	}
}
