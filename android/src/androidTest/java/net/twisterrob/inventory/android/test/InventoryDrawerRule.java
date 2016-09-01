package net.twisterrob.inventory.android.test;

import android.support.test.rule.ActivityTestRule;
import android.support.v4.view.GravityCompat;

import net.twisterrob.android.test.espresso.idle.DrawerIdlingResource;
import net.twisterrob.android.test.espresso.idle.DrawerIdlingResource.ActivityRuleDrawer;
import net.twisterrob.android.test.junit.IdlingResourceRule;
import net.twisterrob.inventory.android.R;

public class InventoryDrawerRule extends IdlingResourceRule {
	public InventoryDrawerRule(ActivityTestRule<?> activity) {
		super(new DrawerIdlingResource(new ActivityRuleDrawer(activity, R.id.drawer), GravityCompat.START));
	}
}
