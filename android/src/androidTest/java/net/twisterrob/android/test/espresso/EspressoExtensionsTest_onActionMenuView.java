package net.twisterrob.android.test.espresso;

import java.lang.reflect.*;

import javax.inject.Provider;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.test.espresso.*;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.*;

import static android.os.Build.VERSION_CODES.*;
import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.junit.TestPackageIntentRule;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.java.utils.ReflectionTools.*;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = VERSION_CODES.HONEYCOMB)
public class EspressoExtensionsTest_onActionMenuView {
	private static final String TEST_ACTION_ITEM = "Test Action Item";
	@Rule public ActivityTestRule<TestActivity> activity = new TestPackageIntentRule<>(TestActivity.class);

	@Test(expected = IllegalStateException.class)
	public void testOversleep() throws Exception {
		// see android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu()
		// pressMenuKey() can't oversleep because it's a key, not a touchDown followed by a touchUp
		assumeThat("app target SDK version need to be newer than Gingerbread to have overflow menu",
				getTargetContext().getApplicationInfo().targetSdkVersion, greaterThanOrEqualTo(HONEYCOMB));
		Method hasVirtualOverflowButton =
				ensureAccessible(Espresso.class.getDeclaredMethod("hasVirtualOverflowButton", Context.class));
		assumeThat("device expected to have an action bar overflow button",
				(Boolean)hasVirtualOverflowButton.invoke(null, getTargetContext()), is(true));

		Field BASE =
				ensureAccessible(findDeclaredField(Espresso.class, "BASE"));
		Field uiControllerProvider =
				ensureAccessible(findDeclaredField(DaggerBaseLayerComponent.class, "provideUiControllerProvider"));
		// originalProvider = ((DaggerBaseLayerComponent)Espresso.BASE).provideUiControllerProvider
		BaseLayerComponent baseLayer = (BaseLayerComponent)BASE.get(null);
		@SuppressWarnings("unchecked") final Provider<UiController> originalProvider
				= (Provider<UiController>)uiControllerProvider.get(baseLayer);

		// setup: make sure UiController oversleeps
		uiControllerProvider.set(baseLayer, new Provider<UiController>() {
			@Override public UiController get() {
				return new OversleepingUiControllerWrapper(originalProvider.get());
			}
		});

		try {
			onActionMenuView(withText(TEST_ACTION_ITEM))
					.check(matches(anything()))
			;
		} finally {
			// reset original for later tests
			uiControllerProvider.set(baseLayer, originalProvider);
		}
	}

	@Test public void testWorking() throws Exception {
		TestActivity activity = this.activity.getActivity();
		activity.itemClicked = false;

		onActionMenuView(withText(TEST_ACTION_ITEM))
				.check(matches(isCompletelyDisplayed()))
				.perform(click())
		;

		assertThat(activity.itemClicked, is(true));
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	@RequiresApi(VERSION_CODES.HONEYCOMB)
	public static class TestActivity extends Activity {
		private static final int ITEM_ID = 3453;
		public boolean itemClicked;
		@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
		@Override public boolean onOptionsItemSelected(MenuItem item) {
			if (item.getItemId() == ITEM_ID) {
				itemClicked = true;
			}
			return super.onOptionsItemSelected(item);
		}
		@Override public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			MenuItem item = menu.add(0, ITEM_ID, 0, TEST_ACTION_ITEM);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			return true;
		}
	}

	private static class OversleepingUiControllerWrapper implements UiController {
		private final UiController hack;
		public OversleepingUiControllerWrapper(UiController hack) {
			this.hack = hack;
		}
		@Override public boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
			boolean result = hack.injectMotionEvent(event);
			// this is where MotionEvents.sendDown would be sleeping a little, but sometimes oversleeps
			try {
				// make sure we really oversleep this time
				Thread.sleep(ViewConfiguration.getLongPressTimeout() * 2);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
			return result;
		}
		@Override public boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException {
			return hack.injectKeyEvent(event);
		}
		@Override public boolean injectString(String str) throws InjectEventSecurityException {
			return hack.injectString(str);
		}
		@Override public void loopMainThreadUntilIdle() {
			hack.loopMainThreadUntilIdle();
		}
		@Override public void loopMainThreadForAtLeast(long millisDelay) {
			hack.loopMainThreadForAtLeast(millisDelay);
		}
	}
}
