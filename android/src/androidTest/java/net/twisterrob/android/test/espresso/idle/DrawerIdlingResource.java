package net.twisterrob.android.test.espresso.idle;

import org.hamcrest.Matcher;

import android.app.Activity;
import android.support.annotation.*;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.view.View;

import static android.support.test.InstrumentationRegistry.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.util.TreeIterables.*;

import net.twisterrob.android.annotation.GravityFlag;
import net.twisterrob.android.test.automators.ActivityCollector;
import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.android.utils.log.LoggingDrawerListener;
import net.twisterrob.android.view.ViewProvider;

import static net.twisterrob.android.test.espresso.DrawerMatchers.*;

public class DrawerIdlingResource extends AsyncIdlingResource {
	private final int drawerGravity;
	private final ViewProvider drawerProvider;
	public DrawerIdlingResource(@NonNull ViewProvider drawerProvider, int drawerGravity) {
		this.drawerProvider = drawerProvider;
		this.drawerGravity = drawerGravity;
	}
	@Override public String getName() {
		return GravityFlag.Converter.toString(drawerGravity) + " drawer";
	}
	private LoggingDrawerListener logger = new LoggingDrawerListener();
	@Override protected boolean isIdle() {
		DrawerLayout drawer = (DrawerLayout)drawerProvider.getView();
		if (drawer != null) {
			drawer.removeDrawerListener(logger);
			drawer.addDrawerListener(logger);
		}
		return drawer == null || !drawer.isDrawerVisible(drawerGravity) || drawer.isDrawerOpen(drawerGravity);
	}
	@Override protected void waitForIdleAsync() {
		final DrawerLayout drawer = (DrawerLayout)drawerProvider.getView();
		if (drawer != null) {
			drawer.addDrawerListener(new SimpleDrawerListener() {
				@Override public void onDrawerStateChanged(int newState) {
					super.onDrawerStateChanged(newState);
					if (newState == DrawerLayout.STATE_IDLE) {
						onTransitionToIdle();
						drawer.removeDrawerListener(this);
					}
				}
			});
		}
	}

	public static IdlingResourceRule rule() {
		return rule(new TopDrawer(), GravityCompat.START);
	}
	public static IdlingResourceRule rule(ActivityTestRule<?> activity) {
		return rule(new ActivityRuleDrawer(activity), GravityCompat.START);
	}
	public static IdlingResourceRule rule(ActivityTestRule<?> activity, @IdRes int drawerId) {
		return rule(new ActivityRuleDrawerById(activity, drawerId), GravityCompat.START);
	}
	public static IdlingResourceRule rule(ViewProvider drawerProvider, @GravityFlag int gravity) {
		return new IdlingResourceRule(new DrawerIdlingResource(drawerProvider, gravity));
	}

	@VisibleForTesting static class TopDrawer implements ViewProvider {
		private final ActivityCollector activities = new ActivityCollector(getInstrumentation());

		public TopDrawer() {
			activities.start();
		}

		@Override public View getView() {
			Activity currentActivity = activities.getLatestResumed();
			if (currentActivity == null) {
				return null;
			}
			Matcher<View> isDrawer = isDrawerLayout();
			View rootView = currentActivity.getWindow().getDecorView();
			for (View view : breadthFirstViewTraversal(rootView)) {
				if (isDrawer.matches(view)) {
					return view;
				}
			}
			return null;
		}

		@Override protected void finalize() throws Throwable {
			super.finalize();
			activities.stop();
		}
	}

	@VisibleForTesting static class ActivityRuleDrawer implements ViewProvider {
		private final ActivityTestRule<?> activity;

		public ActivityRuleDrawer(ActivityTestRule<?> activity) {
			this.activity = activity;
		}

		@Override public View getView() {
			Activity currentActivity = activity.getActivity();
			if (currentActivity == null) {
				return null;
			}
			Matcher<View> isDrawer = isDrawerLayout();
			View rootView = currentActivity.getWindow().getDecorView();
			for (View view : breadthFirstViewTraversal(rootView)) {
				if (isDrawer.matches(view)) {
					return view;
				}
			}
			throw new NoMatchingViewException.Builder().withViewMatcher(isDrawer).withRootView(rootView).build();
		}
	}

	@VisibleForTesting static class ActivityRuleDrawerById implements ViewProvider {
		private final ActivityTestRule<?> activity;
		private final int drawerId;

		public ActivityRuleDrawerById(ActivityTestRule<?> activity, @IdRes int drawerId) {
			this.activity = activity;
			this.drawerId = drawerId;
		}

		@Override public View getView() {
			Activity currentActivity = activity.getActivity();
			if (currentActivity == null) {
				return null;
			}
			View view = currentActivity.findViewById(drawerId);
			if (view == null) {
				View rootView = currentActivity.getWindow().getDecorView();
				Matcher<View> fakeMatcher = withId(drawerId);
				fakeMatcher.matches(rootView); // give Resources to the matcher
				throw new NoMatchingViewException.Builder()
						.withViewMatcher(fakeMatcher)
						.withRootView(rootView)
						.build();
			}
			return view;
		}
	}
}
