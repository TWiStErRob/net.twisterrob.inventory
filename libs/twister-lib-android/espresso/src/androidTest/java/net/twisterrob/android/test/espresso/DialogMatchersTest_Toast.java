package net.twisterrob.android.test.espresso;

import java.util.concurrent.*;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.internal.matchers.ThrowableMessageMatcher.*;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingRootException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Toast;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import junit.framework.AssertionFailedError;

import net.twisterrob.android.test.junit.*;
import net.twisterrob.inventory.android.test.activity.TestActivity;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.DialogMatchersTest.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.test.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DialogMatchersTest_Toast {
	@Rule public final ActivityTestRule<TestActivity> activity = new TestPackageIntentRule<>(TestActivity.class);

	private Toast shownToast;

	@Before public void preconditions() {
		onView(isRoot()).perform(waitForToastsToDisappear());
	}

	@After public void cancelToast() {
		// to reduce possibility of tests interacting
		if (shownToast != null) {
			shownToast.cancel();
		}
	}

	@Test(timeout = DIALOG_TIMEOUT) public void testAssertNoToastIsDisplayed_passes_whenNoToastShown() {
		assertNoToastIsDisplayed();
	}

	@Test(timeout = DIALOG_TIMEOUT) public void testIsToast_finds_shownToast() {
		Toast toast = createToast("Hello Toast!");
		assertNoToastIsDisplayed();
		show(toast);

		try {
			onRoot(isToast()).check(matches(isDisplayed()));
		} finally {
			toast.cancel();
		}

		assertNoToastIsDisplayed();
	}

	@LargeTest
	@Test(timeout = ESPRESSO_BACKOFF_TIMEOUT)
	public void testIsToast_onRootFails_whenNoToastShown() {
		assertNoToastIsDisplayed();

		NoMatchingRootException expectedFailure = assertThrows(NoMatchingRootException.class, new ThrowingRunnable() {
			@Override public void run() {
				onRoot(isToast()).check(matches(isDisplayed()));
			}
		});

		assertThat(expectedFailure,
				hasMessage(startsWith("Matcher 'is toast' did not match any of the following roots")));
	}

	@Test(timeout = DIALOG_TIMEOUT) public void testIsToast_worksAsRootMatcher() {
		Toast toast = createToast("Hello Toast!");
		assertNoToastIsDisplayed();
		show(toast);

		try {
			onView(isDialogMessage())
					.inRoot(isToast())
					.check(matches(withText(containsStringIgnoringCase("hello"))));
		} finally {
			toast.cancel();
		}

		assertNoToastIsDisplayed();
	}

	@LargeTest
	@Test(timeout = ESPRESSO_BACKOFF_TIMEOUT)
	public void testIsToast_inRootFails_whenNoToastShown() {
		assertNoToastIsDisplayed();

		NoMatchingRootException expectedFailure = assertThrows(NoMatchingRootException.class, new ThrowingRunnable() {
			@Override public void run() {
				onView(isDialogMessage())
						.inRoot(isToast())
						.check(matches(anything()));
			}
		});

		assertThat(expectedFailure,
				hasMessage(startsWith("Matcher 'is toast' did not match any of the following roots")));
	}

	@Test(timeout = DIALOG_TIMEOUT) public void testAssertNoToastIsDisplayed_fails_whenToastShown() {
		Toast toast = createToast("Dummy message");
		show(toast);

		AssertionFailedError expectedFailure = assertThrows(AssertionFailedError.class, new ThrowingRunnable() {
			// android.support.test.espresso.base.DefaultFailureHandler$AssertionFailedWithCauseError:
			// 'not toast root existed' doesn't match the selected view.
			// Expected: not toast root existed
			// Got: "LinearLayout{...}"
			// or
			// android.support.test.espresso.base.DefaultFailureHandler$AssertionFailedWithCauseError:
			// 'not toast root existed' doesn't match the selected view.
			// Expected: not toast root existed
			// Got: "TextView{text=Hello Toast!}"
			// at android.support.test.espresso.ViewInteraction.check(ViewInteraction.java:158)
			// at net.twisterrob.android.test.espresso.DialogMatchers.assertNoToastIsDisplayed(DialogMatchers.java:68)
			@Override public void run() {
				assertNoToastIsDisplayed();
			}
		});

		assertThat(expectedFailure, hasMessage(startsWith("View is present in the hierarchy")));
	}

	@Test public void testWaitForToastsToDisappear_waitsForToast() {
		Toast toast = createToast("A toast");
		toast.setDuration(Toast.LENGTH_SHORT);
		show(toast);
		onRoot(isToast()).check(matches(isDisplayed()));

		// based on com.android.server.notification.NotificationManagerService#SHORT_DELAY = 2000
		// allow a bit more than double the set duration to show and hide the toast
		// for some reason the delay is 2000, but on emulator it disappears in ~1900 ms
		assertTimeout(1500, 2 * 2000, TimeUnit.MILLISECONDS, new Runnable() {
			@Override public void run() {
				onView(isRoot()).perform(waitForToastsToDisappear());
			}
		});

		assertNoToastIsDisplayed();
	}

	private static Toast createToast(final @NonNull CharSequence message) {
		return InstrumentationExtensions.callOnMain(new Callable<Toast>() {
			@Override public Toast call() {
				return Toast.makeText(InstrumentationRegistry.getContext(), message, Toast.LENGTH_LONG);
			}
		});
	}

	private void show(@NonNull Toast toast) {
		toast.show();
		shownToast = toast;
		// wait for it to show, this is best effort to reduce flakyness
		onRoot().perform(loopMainThreadUntilIdle());
	}
}
