package net.twisterrob.android.test.espresso;

import java.util.concurrent.Callable;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.internal.matchers.ThrowableMessageMatcher.*;

import android.support.test.rule.ActivityTestRule;
import android.widget.Toast;

import junitparams.JUnitParamsRunner;

import net.twisterrob.android.test.junit.*;
import net.twisterrob.inventory.android.test.activity.TestActivityCompat;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;

@RunWith(JUnitParamsRunner.class)
public class DialogMatchersTest {
	/**
	 * Something is really wrong if it takes more than 5 seconds to show and dismiss a dialog.
	 * It takes 1.3 seconds on average with a few ~3 second outliers on an x86 emulator on a x64 i7-29630QM machine. 
	 */
	static final long DIALOG_TIMEOUT = 5000;

	@Rule public final ActivityTestRule<TestActivityCompat> activity =
			new TestPackageIntentRule<>(TestActivityCompat.class);

	private Toast showToast() {
		return InstrumentationExtensions.callOnMain(new Callable<Toast>() {
			@Override public Toast call() {
				return Toast.makeText(activity.getActivity(), "This is not a dialog!", Toast.LENGTH_LONG);
			}
		});
	}
	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenToastIsVisible() {
		Toast toast = showToast();

		assertNoDialogIsDisplayed();

		toast.cancel();
	}
	@Test(timeout = DIALOG_TIMEOUT)
	public void testNoDialogIsDisplayedWhenToastIsVisible_fail() {
		Toast toast = showToast();

		AssertionError expectedFailure = assertThrows(AssertionError.class, new ThrowingRunnable() {
			@Override public void run() {
				assertDialogIsDisplayed();
			}
		});

		toast.cancel();
		assertThat(expectedFailure, hasMessage(containsString("'is a root view and matches is dialog'")));
	}
}
