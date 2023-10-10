package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.actors.MainActivityActor.Navigator.HomeRoomsActor;
import net.twisterrob.inventory.android.test.categories.*;

@RunWith(AndroidJUnit4.class)
public class ImageLoadingTest {
	
	@SuppressWarnings("deprecation")
	@Rule public final androidx.test.rule.ActivityTestRule<MainActivity> activity
			= new InventoryActivityRule<MainActivity>(MainActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			App.db().resetToTest();
		}
	};

	private final MainActivityActor main = new MainActivityActor();

	@Category({UseCase.Complex.class, On.Main.class, On.Item.class})
	@Test(timeout = 30 * 1000)
	public void test() {
		MainActivityActor.Navigator home = main.assertHomeScreen();
		HomeRoomsActor rooms = home.rooms();
		rooms.assertExists("!All Categories");
		RoomViewActivityActor room = rooms.open("!All Categories");
		// Pick the item before last, because the last one might overlap with the + button.
		// See https://github.com/TWiStErRob/net.twisterrob.inventory/issues/274
		GridBelongingActor item = room.item("Vehicle trailer");
		item.assertHasImage();
		item.assertHasTypeImage();
		item.changeCategory().cancel();
		room.close();
		main.assertHomeScreen();
	}
}
