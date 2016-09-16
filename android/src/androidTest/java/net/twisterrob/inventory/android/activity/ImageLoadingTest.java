package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.*;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.recyclerview.RecyclerViewDataInteraction;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.test.InventoryActivityRule;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

@RunWith(AndroidJUnit4.class)
public class ImageLoadingTest {
	private static final Logger LOG = LoggerFactory.getLogger(ImageLoadingTest.class);
	@Rule public final ActivityTestRule<MainActivity> activity
			= new InventoryActivityRule<MainActivity>(MainActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			Database.resetToTest();
		}
	};

	@Test public void test() {
		onRecyclerItem(withText("!All Categories"))
				.inAdapterView(withId(R.id.rooms))
				.onChildView(withId(R.id.image))
				.check(matches(hasImage()))
				.perform(click());
		RecyclerViewDataInteraction lastItem = onRecyclerItem(withText("Vehicle water"));
		lastItem.onChildView(withId(R.id.image)).check(matches(hasImage()));
		lastItem.onChildView(withId(R.id.type)).check(matches(hasImage()));
		lastItem.perform(click()); // TODO this will bring up the type editor, not the item
		Espresso.pressBack(); // don't leak the dialog
	}
}
