package net.twisterrob.android.test.espresso;

import java.util.concurrent.Callable;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Toast;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.junit.*;
import net.twisterrob.inventory.android.test.activity.TestActivity;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;

@RunWith(AndroidJUnit4.class)
public class DialogMatchersTest_Toast {
	@Rule public final ActivityTestRule<TestActivity> activity = new TestPackageIntentRule<>(TestActivity.class);
	@Rule public ExpectedException thrown = ExpectedException.none();

	@Before public void preconditions() {
		onView(isRoot()).perform(waitForToastsToDisappear());
	}

	@Test public void testToastMessage() {
		Toast toast = createToast("Hello Toast!");
		assertNoToastIsDisplayed();
		toast.show();
		// wait for it to show, this is best effort to reduce flakyness
		onRoot().perform(loopMainThreadUntilIdle());
		onView(withId(android.R.id.message))
				.inRoot(isToast())
				.check(matches(withText(containsStringIgnoringCase("hello"))));
		toast.cancel();
		assertNoToastIsDisplayed();
	}

	@Test public void testToastNotShown() {
		// android.support.test.espresso.base.DefaultFailureHandler$AssertionFailedWithCauseError:
		// 'not toast root existed' doesn't match the selected view.
		// Expected: not toast root existed
		// Got: "LinearLayout{...}"
		createToast("Dummy message").show();
		thrown.expect(junit.framework.AssertionFailedError.class);
		thrown.expectMessage(containsStringIgnoringCase("toast"));
		assertNoToastIsDisplayed();
	}

	private Toast createToast(final String message) {
		return InstrumentationExtensions.callOnMain(new Callable<Toast>() {
			@Override public Toast call() throws Exception {
				return Toast.makeText(InstrumentationRegistry.getContext(), message, Toast.LENGTH_LONG);
			}
		});
	}
}
