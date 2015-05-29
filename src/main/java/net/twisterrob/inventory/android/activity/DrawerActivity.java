package net.twisterrob.inventory.android.activity;

import java.util.*;

import org.slf4j.*;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.*;
import android.widget.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.adapters.IconedItemAdapter;

import static net.twisterrob.inventory.android.activity.MainActivity.*;

// TODO extract as composit class not inheritance
public class DrawerActivity extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(DrawerActivity.class);
	protected ActionBarDrawerToggle mDrawerToggle;
	protected DrawerLayout mDrawerLayout;
	protected View mDrawerLeft;
	protected View mDrawerRight;

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		// TODO still shows up when drawer is up
		return super.onCreateOptionsMenu(menu) && !isDrawerShown();
	}

	@Override public void onContentChanged() {
		super.onContentChanged();

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
		if (mDrawerLayout != null) {
			mDrawerToggle = new StandardMyActionBarDrawerToggle(this, mDrawerLayout);
			mDrawerLayout.setDrawerListener(mDrawerToggle);

			initDrawers();
		}
	}

	private void initDrawers() {
		createDrawerLeft();
		if (mDrawerLeft == null) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}
		createDrawerRight();
		if (mDrawerRight == null) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);
		}
		refreshDrawers(getIntent());
	}

	/** Initialize mDrawerLeft */
	protected void createDrawerLeft() {
		mDrawerLeft = getLayoutInflater().inflate(R.layout.inc_drawer_left_main, mDrawerLayout);
		ListView list = (ListView)mDrawerLeft.findViewById(R.id.drawer_left_list);
		IconedItemAdapter adapter = new IconedItemAdapter(this, R.layout.item_drawer_left, createActions());
		list.setAdapter(adapter);
		list.setOnItemClickListener(new IconedItem.OnClick() {
			@Override public void onItemClick(AdapterView parent, View view, int position, long id) {
				super.onItemClick(parent, view, position, id);
				mDrawerLayout.closeDrawer(parent);
			}
		});
	}

	/** Initialize mDrawerRight */
	protected void createDrawerRight() {

	}

	private int findActivePosition(ListView drawerLeft, Intent intent) {
		for (int pos = 0; pos < drawerLeft.getAdapter().getCount(); pos++) {
			Object item = drawerLeft.getItemAtPosition(pos);
			if (item instanceof SVGIntentItem) {
				SVGIntentItem intentItem = (SVGIntentItem)item;
				if (intent.getComponent().equals(intentItem.getIntent().getComponent())
						&& AndroidTools.toString(intent.getExtras())
						               .equals(AndroidTools.toString(intentItem.getIntent().getExtras()))) {
					return pos;
				}
			}
		}
		return -1;
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
		ListView list = (ListView)mDrawerLeft.findViewById(R.id.drawer_left_list);
		IconedItemAdapter adapter = (IconedItemAdapter)list.getAdapter();
		adapter.setActive(findActivePosition(list, intent));
		adapter.notifyDataSetChanged();
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

	private Collection<IconedItem> createActions() {
		Collection<IconedItem> acts = new ArrayList<>();

		final BaseActivity activity = this;

		// @formatter:off
		acts.add(new SVGIntentItem(R.string.home_title, R.raw.property_home, activity, MainActivity.home()));
		acts.add(new SVGIntentItem(R.string.category_list, R.raw.category_unknown, activity, MainActivity.list(PAGE_CATEGORIES)));
		acts.add(new SVGIntentItem(R.string.property_list, R.raw.property_unknown, activity, MainActivity.list(PAGE_PROPERTIES)));
		acts.add(new SVGIntentItem(R.string.room_list, R.raw.room_unknown, activity, MainActivity.list(PAGE_ROOMS)));
		acts.add(new SVGIntentItem(R.string.item_list, R.raw.category_box, activity, MainActivity.list(PAGE_ITEMS)));
		acts.add(new SVGIntentItem(R.string.sunburst_title, R.raw.ic_sunburst, activity, MainActivity.list(PAGE_SUNBURST)));
		acts.add(new SVGIntentItem(R.string.category_help, R.raw.category_paper, activity, MainActivity.list(PAGE_CATEGORY_HELP)));
		acts.add(new SVGIntentItem(R.string.backup_title, R.raw.category_disc, activity, BackupActivity.chooser()));
		// @formatter:on
		acts.add(new ResourceItem(R.string.pref_activity_title, android.R.drawable.ic_menu_preferences) {
			@Override public void onClick() {
				activity.startActivity(PreferencesActivity.show());
			}
		});

		return acts;
	}

	protected boolean hasDrawer() {
		return mDrawerLayout != null && mDrawerToggle != null && (mDrawerLeft != null || mDrawerRight != null);
	}

	protected boolean isDrawerShown() {
		return hasDrawer()
				&& (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END));
	}

	private static class StandardMyActionBarDrawerToggle extends ActionBarDrawerToggle {
		private final DrawerActivity activity;

		public StandardMyActionBarDrawerToggle(DrawerActivity activity, DrawerLayout drawerLayout) {
			super(activity, drawerLayout, AndroidTools.INVALID_RESOURCE_ID, AndroidTools.INVALID_RESOURCE_ID);
			this.activity = activity;
		}
		@Override
		public void onDrawerClosed(View view) {
			super.onDrawerClosed(view);
			activity.getSupportActionBar().setTitle(activity.getTitle());
			activity.supportInvalidateOptionsMenu();
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			super.onDrawerOpened(drawerView);
			activity.getSupportActionBar().setTitle(activity.getText(R.string.navigation_title));
			activity.supportInvalidateOptionsMenu();
		}
	}
}
