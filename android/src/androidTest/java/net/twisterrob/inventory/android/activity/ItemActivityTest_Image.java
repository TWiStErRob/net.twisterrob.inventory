package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.twisterrob.inventory.android.activity.data.RoomViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
public class ItemActivityTest_Image {
	@Rule public TemporaryFolder temp = new TemporaryFolder();
	@Rule public TestName name = new TestName();

	@Rule public final ActivityTestRule<RoomViewActivity> activity
			= new InventoryActivityRule<RoomViewActivity>(RoomViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long propertyID = db.createProperty(TEST_PROPERTY);
			long roomID = db.createRoom(propertyID, TEST_ROOM);
			getStartIntent().putExtras(Intents.bundleFromRoom(roomID));
		}
	};
	@Rule public final DataBaseActor db = new DataBaseActor();
	private final RoomViewActivityActor roomView = new RoomViewActivityActor();

	@LargeTest
	@Category({UseCase.Complex.class, On.Item.class, Op.DeletesBelonging.class})
	@Test public void testImageDeletedWithItem() throws IOException {
		ItemEditActivityActor newItem = roomView.addItem();
		newItem.setName(TEST_ITEM);
		newItem.save();

		db.assertHasItem(TEST_ITEM);
		db.assertImageCount(is(0L));

		ItemViewActivityActor itemView = roomView.openItem(TEST_ITEM);
		ItemEditActivityActor editor = itemView.edit();
		editor.takePicture(temp.newFile(), TEST_ITEM + "\n" + name.getMethodName());
		editor.save();

		db.assertHasItem(TEST_ITEM);
		db.assertImageCount(is(1L));

		DeleteDialogActor deleteDialog = itemView.delete();
		deleteDialog.confirm();

		db.assertHasNoItem(TEST_ITEM);
		db.assertImageCount(is(0L));
	}
}
