package net.twisterrob.android.test.espresso.idle;

import android.app.Activity;
import android.support.annotation.*;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.view.View;

import net.twisterrob.android.annotation.GravityFlag;
import net.twisterrob.android.view.ViewProvider;

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
	@Override protected boolean isIdle() {
		DrawerLayout drawer = (DrawerLayout)drawerProvider.getView();
		return drawer == null || !drawer.isDrawerVisible(drawerGravity) || drawer.isDrawerOpen(drawerGravity);
	}
	@Override protected void waitForIdleAsync() {
		final DrawerLayout drawer = (DrawerLayout)drawerProvider.getView();
		if (drawer != null) {
			drawer.addDrawerListener(new SimpleDrawerListener() {
				@Override public void onDrawerStateChanged(int newState) {
					if (newState == DrawerLayout.STATE_IDLE) {
						onTransitionToIdle();
						drawer.removeDrawerListener(this);
					}
				}
			});
		}
	}

	public static class ActivityRuleDrawer implements ViewProvider {
		private final ActivityTestRule<?> activity;
		private final int drawerId;
		public ActivityRuleDrawer(ActivityTestRule<?> activity, @IdRes int drawerId) {
			this.activity = activity;
			this.drawerId = drawerId;
		}
		@Override public View getView() {
			Activity currentActivity = activity.getActivity();
			if (currentActivity == null) {
				return null;
			}
			return currentActivity.findViewById(drawerId);
		}
	}
}
