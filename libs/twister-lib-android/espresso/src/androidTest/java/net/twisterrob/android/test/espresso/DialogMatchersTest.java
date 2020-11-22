package net.twisterrob.android.test.espresso;

import java.lang.AssertionError;
import java.util.concurrent.*;

import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.content.Context;
import android.widget.Toast;

import androidx.test.espresso.base.RootViewPicker;
import androidx.test.rule.ActivityTestRule;
import junitparams.*;
import junitparams.naming.TestCaseName;

import net.twisterrob.android.test.junit.*;
import net.twisterrob.inventory.android.test.activity.TestActivityCompat;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.test.hamcrest.Matchers.*;
import static net.twisterrob.test.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class DialogMatchersTest {

	/**
	 * 60 seconds waiting for a root to appear + 10 seconds to reduce flakyness.
	 * @see RootViewPicker#pickARoot()
	 */
	static final long ESPRESSO_BACKOFF_TIMEOUT = 60000 + 10000;

	/**
	 * Something is really wrong if it takes more than 6 seconds to show and dismiss a dialog.
	 * It takes 1.3 seconds on average with a few ~3 second outliers on an x86 emulator on a x64 i7-29630QM machine. 
	 */
	static final long DIALOG_TIMEOUT = 6000;

	/**
	 * Some matchers should decide quickly about existence of views, use this as their timeout.
	 */
	static final long DECISION_TIMEOUT = 2000;

	private static final String POTENTIAL_DIALOGS = "potential dialogs";
	private static final String POTENTIAL_DIALOGS_NAME =
			"{method}[{index}: positive={0}, negative={1}, neutral={2}, cancellable={3}]";

	private static final Matcher<Throwable> DIALOG_DID_NOT_EXIST_MATCHER = containsCause(hasMessage(startsWith(
			"'is a root view and matches is dialog' doesn't match the selected view.")));
	private static final Matcher<Throwable> DIALOG_EXISTED_MATCHER = containsCause(hasMessage(startsWith(
			"View is present in the hierarchy: DecorView{")));
	private static final Matcher<Throwable> NO_ACTIVITIES_FOUND = containsCause(hasMessage(startsWith(
			"No activities found. Did you forget to launch the activity by calling getActivity() or startActivitySync or similar?")));
	private static final Matcher<Throwable> NO_ROOT_FOUND = containsCause(hasMessage(startsWith(
			"Matcher 'is dialog' did not match any of the following roots: [Root{")));
	private static final Matcher<Throwable> NO_ACTIVITIES_PRECHECK = containsCause(hasMessage(startsWith(
			"No activities in stage RESUMED. Activities are {")));

	@Rule public final ActivityTestRule<TestActivityCompat> activity =
			new TestPackageIntentRule<>(TestActivityCompat.class);

	private void assertDialogIsDisplayed_withTimeout() {
		assertTimeout(DECISION_TIMEOUT, TimeUnit.MILLISECONDS, new Runnable() {
			@Override public void run() {
				assertDialogIsDisplayed();
			}
		});
	}
	private void assertNoDialogIsDisplayed_withTimeout() {
		assertTimeout(DECISION_TIMEOUT, TimeUnit.MILLISECONDS, new Runnable() {
			@Override public void run() {
				assertNoDialogIsDisplayed();
			}
		});
	}

	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenActivityVisible() {
		assertNoDialogIsDisplayed_withTimeout();
	}
	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenActivityVisible_fail() {
		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertDialogIsDisplayed_withTimeout();
			}
		});
		assertThat(expectedFailure, NO_ROOT_FOUND);
	}

	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenActivityIsFinished() {
		activity.getActivity().finish();

		assertNoDialogIsDisplayed_withTimeout();
	}
	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenActivityIsFinished_fail() {
		activity.getActivity().finish();

		AssertionError expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertDialogIsDisplayed_withTimeout();
			}
		});
		assertThat(expectedFailure, NO_ACTIVITIES_PRECHECK);
	}

	private void pauseActivity() {
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override public void run() {
				getInstrumentation().callActivityOnPause(activity.getActivity());
			}
		});
	}
	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenActivityIsPaused() {
		pauseActivity();

		assertNoDialogIsDisplayed_withTimeout();
	}
	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenActivityIsPaused_fail() {
		pauseActivity();

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertDialogIsDisplayed_withTimeout();
			}
		});
		assertThat(expectedFailure, NO_ACTIVITIES_PRECHECK);
	}

	private Toast createToast() {
		return InstrumentationExtensions.callOnMain(new Callable<Toast>() {
			@Override public Toast call() {
				return Toast.makeText(activity.getActivity(), "This is not a dialog!", Toast.LENGTH_LONG);
			}
		});
	}
	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenToastIsVisible() {
		Toast toast = createToast();
		toast.show();

		try {
			assertNoDialogIsDisplayed_withTimeout();
		} finally {
			toast.cancel();
		}
	}
	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenToastIsVisible_fail() {
		Toast toast = createToast();
		toast.show();

		try {
			Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
				@Override public void run() {
					assertDialogIsDisplayed_withTimeout();
				}
			});
			assertThat(expectedFailure, NO_ROOT_FOUND);
		} finally {
			toast.cancel();
		}
	}

	private void displayAppCompatAlertDialog(final boolean positive, final boolean negative, final boolean neutral,
			final boolean cancellable) {
		InstrumentationExtensions.runOnMainIfNecessary(new Runnable() {
			@Override public void run() {
				showAppCompatAlertDialog(activity.getActivity(), positive, negative, neutral, cancellable);
			}
		});
	}
	@Parameters(named = POTENTIAL_DIALOGS)
	@TestCaseName(POTENTIAL_DIALOGS_NAME)
	@Test(timeout = DIALOG_TIMEOUT)
	public void testDialogIsDisplayedForAppCompatAlert(
			final boolean positive, final boolean negative, final boolean neutral, final boolean cancellable) {
		displayAppCompatAlertDialog(positive, negative, neutral, cancellable);

		assertDialogIsDisplayed_withTimeout();
	}
	@Parameters(named = POTENTIAL_DIALOGS)
	@TestCaseName(POTENTIAL_DIALOGS_NAME)
	@Test(timeout = DIALOG_TIMEOUT)
	public void testDialogIsDisplayedForAppCompatAlert_fail(
			final boolean positive, final boolean negative, final boolean neutral, final boolean cancellable) {
		displayAppCompatAlertDialog(positive, negative, neutral, cancellable);

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertNoDialogIsDisplayed_withTimeout();
			}
		});
		assertThat(expectedFailure, DIALOG_EXISTED_MATCHER);
	}

	private void displayAndroidAlertDialog(final boolean positive, final boolean negative, final boolean neutral,
			final boolean cancellable) {
		InstrumentationExtensions.runOnMainIfNecessary(new Runnable() {
			@Override public void run() {
				showAndroidAlertDialog(activity.getActivity(), positive, negative, neutral, cancellable);
			}
		});
	}
	@Parameters(named = POTENTIAL_DIALOGS)
	@TestCaseName(POTENTIAL_DIALOGS_NAME)
	@Test(timeout = DIALOG_TIMEOUT)
	public void testDialogIsDisplayedForAndroidAlert(
			final boolean positive, final boolean negative, final boolean neutral, final boolean cancellable) {
		displayAndroidAlertDialog(positive, negative, neutral, cancellable);

		assertDialogIsDisplayed_withTimeout();
	}
	@Parameters(named = POTENTIAL_DIALOGS)
	@TestCaseName(POTENTIAL_DIALOGS_NAME)
	@Test(timeout = DIALOG_TIMEOUT)
	public void testDialogIsDisplayedForAndroidAlert_fail(
			final boolean positive, final boolean negative, final boolean neutral, final boolean cancellable) {
		displayAndroidAlertDialog(positive, negative, neutral, cancellable);

		Throwable expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertNoDialogIsDisplayed_withTimeout();
			}
		});
		assertThat(expectedFailure, DIALOG_EXISTED_MATCHER);
	}

	@NamedParameters(POTENTIAL_DIALOGS)
	private static Object[][] potentialDialogs() {
		return new Object[][] {
				{false, false, false, false},
				{false, false, false, true},
				{false, false, true, false},
				{false, false, true, true},
				{false, true, false, false},
				{false, true, false, true},
				{false, true, true, false},
				{false, true, true, true},
				{true, false, false, false},
				{true, false, false, true},
				{true, false, true, false},
				{true, false, true, true},
				{true, true, false, false},
				{true, true, false, true},
				{true, true, true, false},
				{true, true, true, true},
		};
	}

	static void showAppCompatAlertDialog(
			Context context, boolean positive, boolean negative, boolean neutral, boolean cancellable) {
		androidx.appcompat.app.AlertDialog.Builder builder =
				new androidx.appcompat.app.AlertDialog.Builder(context);
		if (positive) {
			builder.setPositiveButton("positive", null);
		}
		if (negative) {
			builder.setNegativeButton("negative", null);
		}
		if (neutral) {
			builder.setNeutralButton("neutral", null);
		}
		builder.setCancelable(cancellable);
		builder.show();
	}

	static void showAndroidAlertDialog(
			Context context, boolean positive, boolean negative, boolean neutral, boolean cancellable) {
		android.app.AlertDialog.Builder builder =
				new android.app.AlertDialog.Builder(context);
		if (positive) {
			builder.setPositiveButton("positive", null);
		}
		if (negative) {
			builder.setNegativeButton("negative", null);
		}
		if (neutral) {
			builder.setNeutralButton("neutral", null);
		}
		builder.setCancelable(cancellable);
		builder.show();
	}
}
