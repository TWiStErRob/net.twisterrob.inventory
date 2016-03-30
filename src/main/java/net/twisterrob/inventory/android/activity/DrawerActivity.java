package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.view.DrawerNavigator;

import static net.twisterrob.android.utils.tools.AndroidTools.*;
import static net.twisterrob.inventory.android.activity.MainActivity.*;

// TODO extract as composit class not inheritance
public class DrawerActivity extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(DrawerActivity.class);
	protected ActionBarDrawerToggle mDrawerToggle;
	protected DrawerLayout mDrawerLayout;
	protected View mDrawerLeft;
	protected View mDrawerRight;

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu) && !isDrawerShown();
	}

	@Override public void onContentChanged() {
		super.onContentChanged();

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
		if (mDrawerLayout != null) {
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, INVALID_RESOURCE_ID, INVALID_RESOURCE_ID);
			mDrawerLayout.addDrawerListener(mDrawerToggle);
			mDrawerLayout.addDrawerListener(new TitleUpdater());
			mDrawerLayout.addDrawerListener(new OptionsMenuInvalidator());
			mDrawerLayout.addDrawerListener(new CountUpdater());
			initDrawers();
		}
	}

	@Override protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// handle rotation correctly (rotate when drawer is open)
		if (mDrawerLeft != null && mDrawerLayout.isDrawerOpen(mDrawerLeft)) {
			new TitleUpdater().onDrawerOpened(mDrawerLeft);
		}
		if (mDrawerRight != null && mDrawerLayout.isDrawerOpen(mDrawerRight)) {
			new TitleUpdater().onDrawerOpened(mDrawerRight);
		}
	}
	private void initDrawers() {
		createDrawerLeft();
		if (mDrawerLeft == null) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}
		createDrawerRight();
		if (mDrawerRight == null) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
		}
		refreshDrawers(getIntent());
	}

	/** Initialize mDrawerLeft */
	protected void createDrawerLeft() {
		mDrawerLeft = getLayoutInflater().inflate(R.layout.inc_drawer_left_main, mDrawerLayout);
		mDrawerLeft = mDrawerLeft.findViewById(R.id.drawer_left_list);
		DrawerNavigator nav = createDefaultDrawer(this, (NavigationView)mDrawerLeft);
		nav.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
			@Override public boolean onNavigationItemSelected(MenuItem item) {
				mDrawerLayout.closeDrawer(mDrawerLeft);
				return false;
			}
		});
	}

	/** Initialize mDrawerRight */
	protected void createDrawerRight() {

	}

	protected final void refreshDrawers(Intent intent) {
		if (mDrawerLeft != null) {
			refreshDrawerLeft(intent);
		}
		if (mDrawerRight != null) {
			refreshDrawerRight(intent);
		}
	}

	protected void refreshDrawerLeft(Intent intent) {
		DrawerNavigator.get(mDrawerLeft).select(intent);
	}

	protected void refreshDrawerRight(Intent intent) {

	}

	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (hasDrawer()) {
			mDrawerToggle.syncState(); // Sync the toggle state after onRestoreInstanceState has occurred.
		}
	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		refreshDrawers(intent);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		if (hasDrawer() && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override public void onBackPressed() {
		if (isDrawerShown()) {
			mDrawerLayout.closeDrawers();
			return;
		}
		super.onBackPressed();
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (hasDrawer()) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	protected boolean hasDrawer() {
		return mDrawerLayout != null && mDrawerToggle != null && (mDrawerLeft != null || mDrawerRight != null);
	}

	public boolean isDrawerShown() {
		//@formatter:off
		return hasDrawer() && (
				   mDrawerLeft  != null && mDrawerLayout.isDrawerOpen(mDrawerLeft)
				|| mDrawerRight != null && mDrawerLayout.isDrawerOpen(mDrawerRight)
		);
		//@formatter:on
	}

	public static DrawerNavigator createDefaultDrawer(Activity activity, NavigationView nav) {
		DrawerNavigator data = new DrawerNavigator(nav, activity);
		data.add(R.id.action_drawer_home, R.raw.property_home, MainActivity.home());
		data.add(R.id.action_drawer_categories, R.raw.category_unknown, MainActivity.list(PAGE_CATEGORIES));
		data.add(R.id.action_drawer_properties, R.raw.property_unknown, MainActivity.list(PAGE_PROPERTIES));
		data.add(R.id.action_drawer_rooms, R.raw.room_unknown, MainActivity.list(PAGE_ROOMS));
		data.add(R.id.action_drawer_items, R.raw.category_box, MainActivity.list(PAGE_ITEMS));
		data.add(R.id.action_drawer_sunburst, R.raw.ic_sunburst, MainActivity.list(PAGE_SUNBURST));
		data.add(R.id.action_drawer_category_guide, R.raw.category_paper, MainActivity.list(PAGE_CATEGORY_HELP));
		data.add(R.id.action_drawer_backup, R.raw.category_disc, BackupActivity.chooser());
		data.add(R.id.action_drawer_preferences, R.raw.category_tools, PreferencesActivity.show());
		data.addIcons();
		data.updateCounts();
		return data;
	}

	private class TitleUpdater extends SimpleDrawerListener {
		@Override
		public void onDrawerClosed(View view) {
			getSupportActionBar().setTitle(getTitle());
		}
		@Override
		public void onDrawerOpened(View drawerView) {
			getSupportActionBar().setTitle(getText(R.string.navigation_title));
		}
	}

	private class OptionsMenuInvalidator extends SimpleDrawerListener {
		@Override
		public void onDrawerClosed(View view) {
			supportInvalidateOptionsMenu();
		}
		@Override
		public void onDrawerOpened(View drawerView) {
			supportInvalidateOptionsMenu();
		}
	}

	private class CountUpdater extends SimpleDrawerListener {
		@Override public void onDrawerStateChanged(int newState) {
			if (newState == DrawerLayout.STATE_DRAGGING || newState == DrawerLayout.STATE_SETTLING) {
				DrawerNavigator.get(mDrawerLeft).updateCounts();
			}
		}
	}
}
