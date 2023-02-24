package net.twisterrob.inventory.android.activity;

import org.junit.experimental.categories.Category;

import net.twisterrob.inventory.android.activity.data.RoomEditActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.actors.RoomEditActivityActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@Category({On.Room.class, Op.CreatesBelonging.class})
public class RoomEditActivityTest_Create extends EditActivityTest_Create<RoomEditActivity> {
	private long propertyID;
	public RoomEditActivityTest_Create() {
		super(RoomEditActivity.class, new RoomEditActivityActor(), new BelongingValues(
				TEST_ROOM, TEST_ROOM_OTHER,
				TEST_ROOM_TYPE, TEST_ROOM_TYPE_OTHER, TEST_ROOM_TYPE_DEFAULT) {
			@Override protected DataBaseActor.BelongingAssertions createAssertions(DataBaseActor database) {
				return database.new RoomAssertions();
			}
		});
	}

	@Override protected void createContainers() {
		propertyID = database.createProperty(TEST_PROPERTY);
		activity.getStartIntent().putExtras(Intents.bundleFromProperty(propertyID));
	}

	@Override protected void createDuplicate() {
		database.createRoom(propertyID, TEST_ROOM);
	}
}
