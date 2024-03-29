package net.twisterrob.inventory.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import androidx.annotation.*;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener;

import net.twisterrob.android.activity.AboutActivity;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.space.ManageSpaceActivity;
import net.twisterrob.inventory.android.view.DrawerNavigator;

import static net.twisterrob.android.AndroidConstants.*;
import static net.twisterrob.inventory.android.activity.MainActivity.*;

// CONSIDER extract as composite class not inheritance
public abstract class DrawerActivity extends BaseActivity {

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

	@Override protected void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
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
			@Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				mDrawerLayout.closeDrawer(mDrawerLeft);
				return false;
			}
		});
	}

	/** Initialize mDrawerRight */
	protected void createDrawerRight() {
		// optional override
	}

	protected void refreshDrawers(Intent intent) {
		// optional override
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
		//noinspection SimplifiableIfStatement
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

	@Override public void onConfigurationChanged(@NonNull Configuration newConfig) {
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
		data.add(R.id.action_drawer_home, R.raw.property_house, MainActivity.home(activity));
		data.add(R.id.action_drawer_categories, R.raw.category_unknown, MainActivity.list(activity, PAGE_CATEGORIES));
		data.add(R.id.action_drawer_properties, R.raw.property_unknown, MainActivity.list(activity, PAGE_PROPERTIES));
		data.add(R.id.action_drawer_rooms, R.raw.room_unknown, MainActivity.list(activity, PAGE_ROOMS));
		data.add(R.id.action_drawer_items, R.raw.item_box, MainActivity.list(activity, PAGE_ITEMS));
		data.add(R.id.action_drawer_sunburst, R.raw.ic_sunburst, MainActivity.list(activity, PAGE_SUNBURST));
		data.add(R.id.action_drawer_category_guide, R.raw.item_paper, MainActivity.list(activity, PAGE_CATEGORY_HELP));
		data.add(R.id.action_drawer_backup, R.raw.item_disc, BackupActivity.chooser(activity));
		data.add(R.id.action_drawer_manage, R.raw.item_skull, ManageSpaceActivity.launch(activity));
		data.add(R.id.action_drawer_preferences, R.raw.category_tools, PreferencesActivity.show(activity));
		data.add(R.id.action_drawer_about, R.raw.item_chip, new Intent(activity, AboutActivity.class));
		data.addIcons();
		data.updateCounts();
		return data;
	}

	private class TitleUpdater extends SimpleDrawerListener {
		private CharSequence savedTitle;
		private CharSequence savedSubTitle;
		@Override public void onDrawerClosed(View view) {
			setActionBarTitle(getTitle());
			if (TextUtils.equals(savedTitle, getTitle())) {
				setActionBarSubtitle(savedSubTitle);
			}
			savedSubTitle = null;
		}
		@Override public void onDrawerOpened(View drawerView) {
			savedTitle = getSupportActionBar().getTitle();
			savedSubTitle = getSupportActionBar().getSubtitle();
			setActionBarTitle(getText(R.string.navigation_title));
			setActionBarSubtitle(null);
		}
	}

	private class OptionsMenuInvalidator extends SimpleDrawerListener {
		@Override public void onDrawerClosed(View view) {
			supportInvalidateOptionsMenu();
		}
		@Override public void onDrawerOpened(View drawerView) {
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
