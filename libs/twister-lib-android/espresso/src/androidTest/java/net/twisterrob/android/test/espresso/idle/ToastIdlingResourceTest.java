package net.twisterrob.android.test.espresso.idle;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import android.os.Build.*;
import android.widget.Toast;

import androidx.test.espresso.IdlingResource.ResourceCallback;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.FlakyTest;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

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

	@SuppressWarnings("deprecation")
	@Rule public final androidx.test.rule.ActivityTestRule<TestActivity> activity =
			new TestPackageIntentRule<>(TestActivity.class);

	private final ToastIdlingResource idler = new ToastIdlingResource();
	private final Collection<Toast> toasts = new ArrayList<>();
	private final TransitionRecorder callback = new TransitionRecorder();

	@Before public void setUp() {
		idler.registerIdleTransitionCallback(callback);
		assumeTrue(isIdle());
		assumeFalse(callback.hasTransitionedToIdle());
	}

	@After public void tearDown() {
		for (Toast toast : toasts) {
			toast.cancel();
		}
	}

	@FlakyTest(detail = "Toast.show should be sync, but on API 28 somehow it gets async and isIdle will run before Toast.TN.show is called")
	// Toast.show -> INotificationManager.enqueueToast -> record.callback.show -> Toast.TN.show
	@Test public void testDetectsAToast() {
		Toast toast = createToast("Toast");
		assertTrue(isIdle());
		toast.show();
		assertFalse(isIdle());
	}

	@FlakyTest(detail = "Toast.show should be sync, but on API 28 somehow it gets async and isIdle will run before Toast.TN.show is called")
	// Toast.show -> INotificationManager.enqueueToast -> record.callback.show -> Toast.TN.show
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
		// There's differing behavior between Android versions.
		// (in the next explanation: . means fading in/out, - means cancelled)  
		//
		// Old versions seem to queue the toasts:
		// [..-toast1---]
		//              [---toast2---]
		//  ^ cancel toast2 has no effect yet (not idle), toast1 will be faded out, and then toast2 wouldn't be shown
		//   ^ cancel toast1 has immediate effect (idle), toast2 is cancelled so not shown
		//
		// New versions replace toasts:
		// [.--toast1---]
		//  [.--toast2---] -> at this point toast1 is already cancelled
		//   ^ cancel toast2 has immediate effect (idle), because toast1 is already cancelled

		// Only tested a few version yet, so this condition may be wrong.
		// Known behaviors: 21=true, 28=false, 29=true
		boolean expectQueued = VERSION.SDK_INT != VERSION_CODES.P;
		Toast toast1 = createToast("Toast 1");
		Toast toast2 = createToast("Toast 2");
		assertTrue(isIdle());
		toast1.show();
		toast2.show();
		assertFalse(isIdle());
		toast2.cancel();
		if (expectQueued) {
			assertFalse(isIdle());
		} else {
			assertTrue(isIdle());
		}
		toast1.cancel();
		assertTrue(isIdle());
	}

	@Test public void testBecomesIdleRightAfterToastDisappears() throws InterruptedException {
		showToastAndStartWaitingForIdle();
		assertFalse(callback.hasTransitionedToIdle());
		Thread.sleep(SHORT_DELAY - 500);
		assertFalse(callback.hasTransitionedToIdle());
		Thread.sleep(500);
		assertTrue(callback.hasTransitionedToIdle());
	}

	@Test public void testBecomesIdleRightAfterToastCancelled() {
		showToastAndStartWaitingForIdle().cancel();
		// sometimes cancellation has its effect faster, probably due to async nature of cancel, so don't assert here
		//assertFalse(callback.hasTransitionedToIdle());
		processMainThread();
		assertTrue(callback.hasTransitionedToIdle());
	}

	private Toast showToastAndStartWaitingForIdle() {
		Toast toast = createToast("Toast");
		assertFalse(callback.hasTransitionedToIdle());
		assertTrue(isIdle());
		assertFalse(callback.hasTransitionedToIdle());
		toast.show();
		assertFalse(callback.hasTransitionedToIdle());
		processMainThread();
		assertFalse(callback.hasTransitionedToIdle());
		assertFalse(isIdle());
		assertFalse(callback.hasTransitionedToIdle());
		waitForIdleAsync();
		assertFalse(callback.hasTransitionedToIdle());
		return toast;
	}

	private void processMainThread() {
		onView(isRoot()).perform(EspressoExtensions.loopMainThreadUntilIdle());
	}

	private boolean isIdle() {
		return InstrumentationExtensions.callOnMain(new Callable<Boolean>() {
			@Override public Boolean call() {
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
			@Override public Toast call() {
				return Toast.makeText(InstrumentationRegistry.getInstrumentation().getContext(), message, Toast.LENGTH_SHORT);
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
