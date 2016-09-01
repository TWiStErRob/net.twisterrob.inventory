package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.slf4j.*;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.idle.GlideIdlingResource;
import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.test.InventoryActivityRule;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

@RunWith(AndroidJUnit4.class)
public class ImageLoadingTest {
	private static final Logger LOG = LoggerFactory.getLogger(ImageLoadingTest.class);
	@Rule public final ActivityTestRule<MainActivity> activity
			= new InventoryActivityRule<MainActivity>(MainActivity.class) {
		@Override protected void beforeActivityLaunched() {
			super.beforeActivityLaunched();
			Database.resetToTest();
		}
	};
	@Rule public final TestRule glide = new IdlingResourceRule(new GlideIdlingResource());

	@Test public void test() {
		clickNegativeInDialog();
		onRecyclerItem(withText("!All Categories")).inAdapterView(withId(R.id.rooms)).perform(click());
		getInstrumentation().waitForIdleSync();
		onView(withId(android.R.id.list)).perform(scrollToLast());
		onRecyclerItem(withText("Vehicle water")).onChildView(withId(R.id.type)).perform(click());
		Espresso.pressBack();
	}
}
