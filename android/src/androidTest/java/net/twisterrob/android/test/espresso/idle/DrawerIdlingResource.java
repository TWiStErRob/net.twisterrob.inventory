package net.twisterrob.android.test.espresso.idle;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import org.hamcrest.Matcher;
import org.slf4j.*;

import android.app.Activity;
import android.support.annotation.*;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.Stage;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.*;
import android.view.*;

import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.util.TreeIterables.*;

import net.twisterrob.android.annotation.GravityFlag;
import net.twisterrob.android.test.junit.*;
import net.twisterrob.android.utils.log.LoggingDrawerListener;
import net.twisterrob.android.view.ViewProvider;
import net.twisterrob.java.annotations.DebugHelper;

import static net.twisterrob.android.test.espresso.DrawerMatchers.*;
import static net.twisterrob.android.test.junit.InstrumentationExtensions.*;

public class DrawerIdlingResource extends AsyncIdlingResource {
	private static final Logger LOG = LoggerFactory.getLogger(DrawerIdlingResource.class);

	private final int drawerGravity;
	private final ViewProvider drawerProvider;
	private static final Field openState = reflectOnOpenState();
	private static Field reflectOnOpenState() {
		try {
			Field result = LayoutParams.class.getDeclaredField("openState");
			result.setAccessible(true);
			return result;
		} catch (NoSuchFieldException ex) {
			throw new IllegalStateException("This version of design library doesn't support querying state.", ex);
		}
	}

	public DrawerIdlingResource(@NonNull ViewProvider drawerProvider, int drawerGravity) {
		this.drawerProvider = drawerProvider;
		this.drawerGravity = drawerGravity;
	}
	@Override public String getName() {
		return GravityFlag.Converter.toString(drawerGravity) + " drawer";
	}
	///** Debug helper to log all events happening with a DrawerLayout as soon as we know about it. */
	//private final IdleLogger logger = new IdleLogger();
	@Override protected boolean isIdle() {
		DrawerLayout drawerLayout = (DrawerLayout)drawerProvider.getView();
		//logger.onNewDrawer(drawerLayout);
		if (drawerLayout == null) {
			return true;
		}
		// drawerLayout.isDrawerVisible(drawerGravity) && !drawerLayout.isDrawerOpen(drawerGravity) is not enough
		// right after openDrawer() call nothing is visible and drawer is not open
		for (int i = 0; i < drawerLayout.getChildCount(); ++i) {
			View child = drawerLayout.getChildAt(i);
			DrawerLayout.LayoutParams params = (LayoutParams)child.getLayoutParams();
			if (params.gravity == Gravity.NO_GRAVITY) {
				continue;
			}
			try {
				int state = (Integer)openState.get(params);
				if (state > 1) { // 0 = closed, 1 = opened
					return false; // state flag contains opening or closing
				}
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		return true;
	}
	@Override protected void waitForIdleAsync() {
		final DrawerLayout drawer = (DrawerLayout)drawerProvider.getView();
		if (drawer != null) {
			drawer.addDrawerListener(new SimpleDrawerListener() {
				@Override public void onDrawerStateChanged(int newState) {
					super.onDrawerStateChanged(newState);
					if (newState == DrawerLayout.STATE_IDLE) {
						transitionToIdle();
						drawer.removeDrawerListener(this);
					}
				}
			});
		}
	}

	/**
	 * Create a rule that will watch for any drawers in the topmost resumed activity
	 * and prevent the test from continuing until it's settled in place (open or closed).
	 */
	public static IdlingResourceRule rule() {
		return rule(new TopDrawer(), GravityCompat.START);
	}
	/**
	 * <p><i>Note: this is not viable if the test rotates the activity
	 * or for any other reason the activity may be re-created during test.</i></p>
	 */
	public static IdlingResourceRule rule(ActivityTestRule<?> activity) {
		return rule(new ActivityRuleDrawer(activity), GravityCompat.START);
	}
	/**
	 * <p><i>Note: this is not viable if the test rotates the activity
	 * or for any other reason the activity may be re-created during test.</i></p>
	 */
	public static IdlingResourceRule rule(ActivityTestRule<?> activity, @IdRes int drawerId) {
		return rule(new ActivityRuleDrawerById(activity, drawerId), GravityCompat.START);
	}
	/**
	 * @see TopDrawer
	 * @see ActivityRuleDrawer
	 * @see ActivityRuleDrawerById
	 */
	public static IdlingResourceRule rule(ViewProvider drawerProvider, @GravityFlag int gravity) {
		return new IdlingResourceRule(new DrawerIdlingResource(drawerProvider, gravity));
	}

	public static class TopDrawer implements ViewProvider {
		private WeakReference<Activity> lastActivityRef = new WeakReference<>(null);

		@Override public View getView() {
			Activity currentActivity = InstrumentationExtensions.tryGetActivityInStage(Stage.RESUMED);
			if (currentActivity == null) {
				LOG.warn("No resumed activity in {}", getAllActivities());
				return null;
			}
			Activity lastActivity = lastActivityRef.get();
			lastActivityRef = new WeakReference<>(currentActivity);

			Matcher<View> isDrawer = isDrawerLayout();
			View rootView = currentActivity.getWindow().getDecorView();
			for (View view : breadthFirstViewTraversal(rootView)) {
				if (isDrawer.matches(view)) {
					return view;
				}
			}
			if (lastActivity != currentActivity) {
				LOG.warn("No DrawerLayout found in {}", currentActivity, new NoMatchingViewException.Builder()
						.withRootView(rootView)
						.withViewMatcher(isDrawer)
						.build());
			}
			return null;
		}
	}

	public static class ActivityRuleDrawer implements ViewProvider {
		private final ActivityTestRule<?> activity;

		public ActivityRuleDrawer(ActivityTestRule<?> activity) {
			this.activity = activity;
		}

		@Override public View getView() {
			Activity currentActivity = activity.getActivity();
			if (currentActivity == null
					|| InstrumentationExtensions.getActivityStage(currentActivity) != Stage.RESUMED) {
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

	public static class ActivityRuleDrawerById implements ViewProvider {
		private final ActivityTestRule<?> activity;
		private final int drawerId;

		public ActivityRuleDrawerById(ActivityTestRule<?> activity, @IdRes int drawerId) {
			this.activity = activity;
			this.drawerId = drawerId;
		}

		@Override public View getView() {
			Activity currentActivity = activity.getActivity();
			if (currentActivity == null
					|| InstrumentationExtensions.getActivityStage(currentActivity) != Stage.RESUMED) {
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

	@DebugHelper
	@VisibleForTesting static class IdleLogger extends LoggingDrawerListener {
		private WeakReference<DrawerLayout> lastDrawerLayout = new WeakReference<>(null);
		public void onNewDrawer(DrawerLayout newDrawer) {
			DrawerLayout lastDrawer = lastDrawerLayout.get();
			if (lastDrawer != newDrawer) {
				if (lastDrawer != null) {
					lastDrawer.removeDrawerListener(this);
				} else {
					lastDrawerLayout = new WeakReference<>(newDrawer);
					newDrawer.addDrawerListener(this);
				}
			}
		}
	}
}
