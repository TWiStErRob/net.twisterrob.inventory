package net.twisterrob.android.test.espresso;

import java.lang.reflect.*;

import javax.inject.Provider;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.*;
import static org.junit.Assert.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.view.*;

import static android.os.Build.VERSION_CODES.*;

import androidx.annotation.*;
import androidx.test.espresso.*;
import androidx.test.espresso.base.InterruptableUiController;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.core.app.ApplicationProvider.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.junit.TestPackageIntentRule;
import net.twisterrob.test.junit.FlakyTestException;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.java.utils.ReflectionTools.*;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = VERSION_CODES.HONEYCOMB)
public class EspressoExtensionsTest_onActionMenuView {
	@Rule public final ActivityTestRule<TestActivity> activity = new TestPackageIntentRule<>(TestActivity.class);

	private void verifyOversleepProtection(ThrowingRunnable actionThatTendsToOversleep)
			throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// see androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu()
		// pressMenuKey() can't oversleep because it's a key, not a touchDown followed by a touchUp
		assumeThat("app target SDK version need to be newer than Gingerbread to have overflow menu",
				getApplicationContext().getApplicationInfo().targetSdkVersion, greaterThanOrEqualTo(HONEYCOMB));
		Method hasVirtualOverflowButton =
				ensureAccessible(Espresso.class.getDeclaredMethod("hasVirtualOverflowButton", Context.class));
		assumeThat("device expected to have an action bar overflow button",
				(Boolean)hasVirtualOverflowButton.invoke(null, getApplicationContext()), is(true));

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
				return new OversleepingUiControllerWrapper((InterruptableUiController)originalProvider.get());
			}
		});

		try {
			assertThrows(FlakyTestException.class, actionThatTendsToOversleep);
		} finally {
			// reset original for later tests
			uiControllerProvider.set(baseLayer, originalProvider);
		}
	}

	@Test public void testOversleepWithId() throws Exception {
		verifyOversleepProtection(new ThrowingRunnable() {
			@Override public void run() {
				onActionMenuItem(withMenuItemId(TestActivity.ITEM_ID))
						.check(matches(anything()))
				;
			}
		});
	}
	@Test public void testOversleepWithText() throws Exception {
		verifyOversleepProtection(new ThrowingRunnable() {
			@Override public void run() {
				onActionMenuView(withText(TestActivity.ITEM_LABEL))
						.check(matches(anything()))
				;
			}
		});
	}

	@Test public void testWorkingWithId() {
		TestActivity activity = this.activity.getActivity();
		activity.itemClicked = false;

		onActionMenuItem(withMenuItemId(TestActivity.ITEM_ID))
				.check(matches(isCompletelyDisplayed()))
				.perform(click())
		;

		assertThat(activity.itemClicked, is(true));
	}

	@Test public void testWorkingWithText() {
		TestActivity activity = this.activity.getActivity();
		activity.itemClicked = false;

		onActionMenuView(withText(TestActivity.ITEM_LABEL))
				.check(matches(isCompletelyDisplayed()))
				.perform(click())
		;

		assertThat(activity.itemClicked, is(true));
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	@RequiresApi(VERSION_CODES.HONEYCOMB)
	public static class TestActivity extends Activity {

		private static final String ITEM_LABEL = "Test Action Item";
		private static final int ITEM_ID = (int)(Math.random() * Integer.MAX_VALUE);

		public boolean itemClicked;

		@Override public boolean onOptionsItemSelected(MenuItem item) {
			if (item.getItemId() == ITEM_ID) {
				itemClicked = true;
			}
			return super.onOptionsItemSelected(item);
		}

		@Override public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			MenuItem item = menu.add(0, ITEM_ID, 0, ITEM_LABEL);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			return true;
		}
	}

	private static class OversleepingUiControllerWrapper implements InterruptableUiController {
		private final @NonNull InterruptableUiController hack;
		public OversleepingUiControllerWrapper(@NonNull InterruptableUiController hack) {
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
		@Override public void interruptEspressoTasks() {
			hack.interruptEspressoTasks();
		}
	}
}
