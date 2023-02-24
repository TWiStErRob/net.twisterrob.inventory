package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.twisterrob.inventory.android.activity.data.PropertyViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Property.class, Op.DeletesBelonging.class})
public class PropertyViewActivityTest_Delete {

	@SuppressWarnings("deprecation")
	@Rule public final androidx.test.rule.ActivityTestRule<PropertyViewActivity> activity
			= new InventoryActivityRule<PropertyViewActivity>(PropertyViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			propertyID = db.createProperty(TEST_PROPERTY);
			getStartIntent().putExtras(Intents.bundleFromProperty(propertyID));
		}
	};
	@Rule public final DataBaseActor db = new AppSingletonDatabaseActor();
	private long propertyID;
	private final PropertyViewActivityActor propertyView = new PropertyViewActivityActor();

	@Before public void preconditionsForDeletingAProperty() {
		db.assertHasProperty(TEST_PROPERTY);
	}
	@After public void closeDialog() {
		attemptCloseDialog();
	}

	@Category({Op.Cancels.class})
	@Test public void testDeleteCancel() {
		DeleteDialogActor deleteDialog = propertyView.delete();
		deleteDialog.cancel();

		db.assertHasProperty(TEST_PROPERTY);
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testDeleteMessage() {
		DeleteDialogActor deleteDialog = propertyView.delete();

		deleteDialog.checkDialogMessage(containsString(TEST_PROPERTY));
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testDeleteMessageWithContents() {
		db.createRoom(propertyID, TEST_ROOM);

		DeleteDialogActor deleteDialog = propertyView.delete();

		deleteDialog.checkDialogMessage(allOf(
				containsString(TEST_PROPERTY),
				containsString(TEST_ROOM)
		));
	}

	@Category({Op.ChecksMessage.class})
	@Test public void testDeleteMessageWithContentsMultiple() {
		db.createRoom(propertyID, TEST_ROOM);
		db.createRoom(propertyID, TEST_ROOM_OTHER);

		DeleteDialogActor deleteDialog = propertyView.delete();

		deleteDialog.checkDialogMessage(allOf(
				containsString(TEST_PROPERTY),
				containsString(TEST_ROOM),
				containsString(TEST_ROOM_OTHER)
		));
	}

	@Test public void testDeleteConfirm() {
		DeleteDialogActor deleteDialog = propertyView.delete();
		deleteDialog.confirm();

		propertyView.assertClosing();
		db.assertHasNoProperty(TEST_PROPERTY);
	}

	@Test public void testDeleteConfirmWithContents() {
		db.createRoom(propertyID, TEST_ROOM);

		DeleteDialogActor deleteDialog = propertyView.delete();
		deleteDialog.confirm();

		propertyView.assertClosing();
		db.assertHasNoProperty(TEST_PROPERTY);
		db.assertHasNoRoom(TEST_ROOM);
	}

	@Category({UseCase.Complex.class})
	@Test public void testDeleteConfirmWithContentsMultiple() {
		db.createRoom(propertyID, TEST_ROOM);
		db.createRoom(propertyID, TEST_ROOM_OTHER);

		DeleteDialogActor deleteDialog = propertyView.delete();
		deleteDialog.confirm();

		propertyView.assertClosing();
		db.assertHasNoProperty(TEST_PROPERTY);
		db.assertHasNoRoom(TEST_ROOM);
		db.assertHasNoRoom(TEST_ROOM_OTHER);
	}
}
