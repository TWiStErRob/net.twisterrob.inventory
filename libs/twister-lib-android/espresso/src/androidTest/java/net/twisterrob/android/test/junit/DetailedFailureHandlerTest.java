package net.twisterrob.android.test.junit;

import org.junit.*;
import org.junit.function.ThrowingRunnable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.*;
import android.support.test.espresso.base.DefaultFailureHandler;
import android.support.test.rule.ActivityTestRule;

import net.twisterrob.android.test.junit.AndroidJUnitRunner.DetailedFailureHandler;
import net.twisterrob.inventory.android.test.activity.TestActivity;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class DetailedFailureHandlerTest {
	@Rule public ActivityTestRule<TestActivity> activity = new TestPackageIntentRule<>(TestActivity.class);

	@Test public void testNoActivityResumedExceptionHasRealCause() {
		DefaultFailureHandler defaultHandler = new DefaultFailureHandler(InstrumentationRegistry.getTargetContext());
		try {
			Espresso.setFailureHandler(new DetailedFailureHandler(defaultHandler));

			Throwable expectedFailure = assertThrows(NoActivityResumedException.class, new ThrowingRunnable() {
				@Override public void run() {
					Espresso.pressBack();
				}
			});

			assertThat(expectedFailure, hasMessage("Pressed back and killed the app"));
			assertThat(expectedFailure, containsStackTrace(
					// Espresso's public cause for this exception
					allOf(stackClass("android.support.test.espresso.ViewInteraction"), stackMethod("perform")),
					allOf(stackClass("android.support.test.espresso.ViewInteraction"),
							stackMethod("waitForAndHandleInteractionResults")),
					// Real cause in Espresso's internals, helps debugging if shown
					allOf(stackClass("android.support.test.espresso.action.PressBackAction"), stackMethod("perform")),
					allOf(stackClass("android.support.test.espresso.action.KeyEventActionBase"),
							stackMethod("waitForPendingForegroundActivities"))
			));
		} finally {
			Espresso.setFailureHandler(defaultHandler);
		}
	}

	@Test public void testDefaultFailureHandlerDoesNotHaveInternalDetails() {
		Throwable expectedFailure = assertThrows(NoActivityResumedException.class, new ThrowingRunnable() {
			@Override public void run() {
				Espresso.pressBack();
			}
		});

		assertThat(expectedFailure, hasMessage("Pressed back and killed the app"));
		assertThat(expectedFailure, containsStackTrace(
				// Espresso's public cause for this exception
				allOf(stackClass("android.support.test.espresso.ViewInteraction"), stackMethod("perform")),
				allOf(stackClass("android.support.test.espresso.ViewInteraction"),
						stackMethod("waitForAndHandleInteractionResults")),
				// Real cause in Espresso's internals, helps debugging if shown
				not(allOf(stackClass("android.support.test.espresso.action.PressBackAction"), stackMethod("perform"))),
				not(allOf(stackClass("android.support.test.espresso.action.KeyEventActionBase"),
						stackMethod("waitForPendingForegroundActivities")))
		));
	}
}
