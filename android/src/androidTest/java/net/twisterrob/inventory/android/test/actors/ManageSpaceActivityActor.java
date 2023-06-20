package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.not;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.test.espresso.matcher.BoundedMatcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.space.ManageSpaceActivity;

import static net.twisterrob.android.test.espresso.DialogMatchers.assertNoDialogIsDisplayed;
import static net.twisterrob.android.test.espresso.DialogMatchers.clickNegativeInDialog;
import static net.twisterrob.android.test.espresso.DialogMatchers.clickPositiveInDialog;

public class ManageSpaceActivityActor extends ActivityActor {
	public ManageSpaceActivityActor() {
		super(ManageSpaceActivity.class);
	}

	public void assertDisplayed() {
		onView(withId(R.id.contents)).check(matches(isDisplayed()));
	}

	public void assertNoProgress() {
		onView(withId(R.id.refresher)).check(matches(not(isRefreshing())));
	}

	public @NonNull RebuildSearchIndexActor rebuildSearchIndex() {
		onView(withId(R.id.storage_search_clear)).perform(scrollTo(), click());
		RebuildSearchIndexActor dialog = new RebuildSearchIndexActor();
		dialog.assertDisplayed();
		return dialog;
	}

	public @NonNull ClearImageCacheActor clearImageCache() {
		onView(withId(R.id.storage_imageCache_clear)).perform(scrollTo(), click());
		ClearImageCacheActor dialog = new ClearImageCacheActor();
		dialog.assertDisplayed();
		return dialog;
	}

	public static class RebuildSearchIndexActor extends ConfirmDialogActor {
	}

	public static class ClearImageCacheActor extends ConfirmDialogActor {
	}

	public abstract static class ConfirmDialogActor extends AlertDialogActor {
		public void confirm() {
			clickPositiveInDialog();
			assertNoDialogIsDisplayed();
		}
		public void cancel() {
			clickNegativeInDialog();
			assertNoDialogIsDisplayed();
		}
	}

	private static @NonNull Matcher<View> isRefreshing() {
		return new BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout.class) {
			@Override public void describeTo(Description description) {
				description.appendText("is refreshing");
			}
			@Override protected boolean matchesSafely(SwipeRefreshLayout item) {
				return item.isRefreshing();
			}
		};
	}
}
