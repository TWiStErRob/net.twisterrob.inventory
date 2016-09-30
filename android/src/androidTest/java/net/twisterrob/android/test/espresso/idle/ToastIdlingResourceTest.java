package net.twisterrob.android.test.espresso.idle;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingResource.ResourceCallback;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Toast;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.EspressoExtensions;
import net.twisterrob.android.test.junit.*;
import net.twisterrob.inventory.android.test.activity.TestActivity;
import net.twisterrob.java.exceptions.StackTrace;

// TODO for UiThreadTest auto-import works, explicit import works, ctrl+space shows nothing
// @UiThread would be nice for the tests, but they need interaction with the UI thread (delayed), so it can't be used,
// instead there are some methods below that call the methods on the UI thread to simulate Espresso's behavior.
@RunWith(AndroidJUnit4.class)
public class ToastIdlingResourceTest {
	// com.android.server.notification.NotificationManagerService#SHORT_DELAY
	private static final long SHORT_DELAY = 2000;

	@Rule public final ActivityTestRule<TestActivity> activity = new TestPackageIntentRule<>(TestActivity.class);
	private final ToastIdlingResource idler = new ToastIdlingResource();
	private final List<Toast> toasts = new ArrayList<>();
	private final TransitionRecorder callback = new TransitionRecorder();

	@Before public void setUp() throws Exception {
		idler.registerIdleTransitionCallback(callback);
		assumeTrue(isIdle());
		assumeFalse(callback.hasTransitionedToIdle());
	}

	@After public void tearDown() {
		for (Toast toast : toasts) {
			toast.cancel();
		}
	}

	@Test public void testDetectsAToast() {
		Toast toast = createToast("Toast");
		assertTrue(isIdle());
		toast.show();
		assertFalse(isIdle());
	}

	@Test public void testDetectsAToastWhenCancelled() {
		Toast toast = createToast("Toast");
		assertTrue(isIdle());
		toast.show();
		assertFalse(isIdle());
		toast.cancel();
		assertTrue(isIdle());
	}

	@SuppressWarnings("Duplicates")
	@Test public void testDetectsMultipleToasts() {
		Toast toast1 = createToast("Toast 1");
		Toast toast2 = createToast("Toast 2");
		assertTrue(isIdle());
		toast1.show();
		toast2.show();
		assertFalse(isIdle());
		toast1.cancel();
		assertFalse(isIdle());
		toast2.cancel();
		assertTrue(isIdle());
	}

	@SuppressWarnings("Duplicates")
	@Test public void testDetectsMultipleToastsCancelledInReverse() {
		Toast toast1 = createToast("Toast 1");
		Toast toast2 = createToast("Toast 2");
		assertTrue(isIdle());
		toast1.show();
		toast2.show();
		assertFalse(isIdle());
		toast2.cancel();
		assertFalse(isIdle());
		toast1.cancel();
		assertTrue(isIdle());
	}

	@Test public void testBecomesIdleRightAfterToastDisappears() throws InterruptedException {
		showToastAndStartWaitingForIdle();
		assertFalse(callback.hasTransitionedToIdle());
		Thread.sleep(SHORT_DELAY);
		assertTrue(callback.hasTransitionedToIdle());
	}

	@Test public void testBecomesIdleRightAfterToastCancelled() {
		showToastAndStartWaitingForIdle().cancel();
		assertFalse(callback.hasTransitionedToIdle());
		onView(isRoot()).perform(EspressoExtensions.loopMainThreadUntilIdle());
		assertTrue(callback.hasTransitionedToIdle());
	}

	private Toast showToastAndStartWaitingForIdle() {
		Toast toast = createToast("Toast");
		assertFalse(callback.hasTransitionedToIdle());
		assertTrue(isIdle());
		assertFalse(callback.hasTransitionedToIdle());
		toast.show();
		assertFalse(callback.hasTransitionedToIdle());
		onView(isRoot()).perform(EspressoExtensions.loopMainThreadUntilIdle());
		assertFalse(callback.hasTransitionedToIdle());
		assertFalse(isIdle());
		assertFalse(callback.hasTransitionedToIdle());
		waitForIdleAsync();
		return toast;
	}

	private boolean isIdle() {
		return InstrumentationExtensions.callOnMain(new Callable<Boolean>() {
			@Override public Boolean call() throws Exception {
				return idler.isIdle();
			}
		});
	}

	private void waitForIdleAsync() {
		InstrumentationExtensions.runOnMain(new Runnable() {
			@Override public void run() {
				idler.waitForIdleAsync();
			}
		});
	}

	private Toast createToast(final String message) {
		Toast toast = InstrumentationExtensions.callOnMain(new Callable<Toast>() {
			@Override public Toast call() throws Exception {
				return Toast.makeText(InstrumentationRegistry.getContext(), message, Toast.LENGTH_SHORT);
			}
		});
		toasts.add(toast);
		return toast;
	}

	private static class TransitionRecorder implements ResourceCallback {
		private final AtomicBoolean transitioned = new AtomicBoolean();
		private StackTrace firstTransition;

		@SuppressWarnings("UnnecessaryInitCause")
		@Override public void onTransitionToIdle() {
			if (!transitioned.compareAndSet(false, true)) {
				AssertionError error = new AssertionError("Transitioned twice.");
				error.initCause(firstTransition);
				throw error;
			} else {
				firstTransition = new StackTrace();
			}
		}

		public boolean hasTransitionedToIdle() {
			return transitioned.get();
		}
	}
}
