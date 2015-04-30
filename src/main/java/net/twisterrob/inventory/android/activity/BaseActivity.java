package net.twisterrob.inventory.android.activity;

import java.lang.annotation.*;
import java.util.Collection;

import org.slf4j.*;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.*;
import android.view.*;
import android.widget.*;

import net.twisterrob.android.activity.BackPressAware;
import net.twisterrob.android.content.glide.*;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.Constants.Pic;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.adapters.IconedItemAdapter;

import static net.twisterrob.inventory.android.activity.BaseActivity.For.Feature.*;
import static net.twisterrob.inventory.android.activity.MainActivity.*;
import static net.twisterrob.java.utils.CollectionTools.*;

public class BaseActivity extends VariantActivity {
	@For(Log) private static final Logger LOG = LoggerFactory.getLogger(BaseActivity.class);
	@For(Drawer) protected ActionBarDrawerToggle mDrawerToggle;
	@For(Drawer) protected DrawerLayout mDrawerLayout;
	@For(Drawer) protected View mDrawerLeft;
	@For(Drawer) protected View mDrawerRight;

	@For(Log) @Override protected void onCreate(Bundle savedInstanceState) {
		LOG.debug("Creating {}@{} {}\n{}",
				getClass().getSimpleName(),
				Integer.toHexString(System.identityHashCode(this)),
				AndroidTools.toLongString(getIntent().getExtras()),
				getIntent()
		);
		super.onCreate(savedInstanceState);
	}

	@Override public boolean onMenuOpened(int featureId, Menu menu) {
		AndroidTools.showActionBarOverflowIcons(featureId, menu, true);
		return super.onMenuOpened(featureId, menu);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		// TODO still shows up when drawer is up
		return super.onCreateOptionsMenu(menu) && !isDrawerShown();
	}

	@For(Drawer) @Override public void onContentChanged() {
		super.onContentChanged();

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
		if (mDrawerLayout != null) {
			mDrawerToggle = new StandardMyActionBarDrawerToggle(this, mDrawerLayout);
			mDrawerLayout.setDrawerListener(mDrawerToggle);

			initDrawers();
		}
	}

	@For(Drawer) private void initDrawers() {
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

	@For(Drawer) private int findActivePosition(ListView drawerLeft, Intent intent) {
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

	@For(Drawer) protected final void refreshDrawers(Intent intent) {
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

	@For(Drawer) protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (hasDrawer()) {
			mDrawerToggle.syncState(); // Sync the toggle state after onRestoreInstanceState has occurred.
		}
	}

	@For(Drawer) @Override protected void onNewIntent(Intent intent) {
		LOG.trace("Refreshing {}@{} {}\n{}",
				getClass().getSimpleName(),
				Integer.toHexString(System.identityHashCode(this)),
				AndroidTools.toString(intent.getExtras()),
				intent
		);
		super.onNewIntent(intent);
		refreshDrawers(intent);
	}

	@For(Drawer) @Override public boolean onOptionsItemSelected(MenuItem item) {
		if (hasDrawer() && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@For(Drawer) @Override public void onBackPressed() {
		if (isDrawerShown()) {
			mDrawerLayout.closeDrawers();
			return;
		}
		for (Fragment fragment : nonNull(getSupportFragmentManager().getFragments())) {
			if (fragment != null && fragment.isAdded()
					&& fragment instanceof BackPressAware && ((BackPressAware)fragment).onBackPressed()) {
				return;
			}
		}
		super.onBackPressed();
	}

	@For(Drawer) @Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (hasDrawer()) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@For(Drawer) protected Collection<IconedItem> createActions() {
		Collection<IconedItem> acts = super.createActions();
		final BaseActivity activity = this;

		// @formatter:off
		acts.add(new SVGIntentItem(R.string.home_title, R.raw.property_home, activity, MainActivity.home()));
		acts.add(new SVGIntentItem(R.string.category_list, R.raw.category_unknown, activity, MainActivity.list(PAGE_CATEGORIES)));
		acts.add(new SVGIntentItem(R.string.property_list, R.raw.property_unknown, activity, MainActivity.list(PAGE_PROPERTIES)));
		acts.add(new SVGIntentItem(R.string.room_list, R.raw.room_unknown, activity, MainActivity.list(PAGE_ROOMS)));
		acts.add(new SVGIntentItem(R.string.item_list, R.raw.category_box, activity, MainActivity.list(PAGE_ITEMS)));
		acts.add(new SVGIntentItem(R.string.sunburst_title, R.raw.ic_sunburst, activity, MainActivity.list(PAGE_SUNBURST)));
		acts.add(new SVGIntentItem(R.string.backup_title, R.raw.category_disc, activity, BackupActivity.chooser()));
		// @formatter:on
		acts.add(new ResourceItem(R.string.pref_activity_title, android.R.drawable.ic_menu_preferences) {
			@Override public void onClick() {
				activity.startActivity(PreferencesActivity.show());
			}
		});

		return acts;
	}

	@For(Drawer) protected boolean hasDrawer() {
		return mDrawerLayout != null && mDrawerToggle != null && (mDrawerLeft != null || mDrawerRight != null);
	}

	@For(Drawer) protected boolean isDrawerShown() {
		return hasDrawer()
				&& (mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END));
	}

	@SuppressWarnings("unchecked")
	@For(Children) protected <T extends Fragment> T getFragment(@IdRes int id) {
		return (T)getSupportFragmentManager().findFragmentById(id);
	}

	@For(Children) public void setActionBarSubtitle(CharSequence string) {
		getSupportActionBar().setSubtitle(string);
	}
	@For(Children) public void setActionBarTitle(CharSequence string) {
		getSupportActionBar().setTitle(string);
	}

	@For(Children) public void setIcon(Drawable iconDrawable) {
		getSupportActionBar().setIcon(iconDrawable);
	}
	@For(Children) public void setIcon(@RawRes int resourceId) {
		Pic.svg()
		   .load(resourceId)
		   .transform(new PaddingTransformation(this, AndroidTools.dipInt(this, 4)))
		   .into(new ActionBarIconTarget(getSupportActionBar()));
	}

	@For(Drawer) private static class StandardMyActionBarDrawerToggle extends ActionBarDrawerToggle {
		private final BaseActivity activity;

		public StandardMyActionBarDrawerToggle(BaseActivity activity, DrawerLayout drawerLayout) {
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

	@Retention(RetentionPolicy.SOURCE) @interface For {
		Feature[] value();
		enum Feature {
			Log,
			Children,
			Drawer,
		}
	}

	/** We kind of know that there's an action bar in all child classes*/
	@Override public @NonNull ActionBar getSupportActionBar() {
		return super.getSupportActionBar();
	}

	@Override public boolean onSupportNavigateUp() {
		if (Intents.isChildNav(getIntent())) {
			onBackPressed();
			return true;
		}
		return super.onSupportNavigateUp();
	}

	/** Workaround for broken up navigation post Jelly Bean?!
	 * @see <a href="http://stackoverflow.com/questions/14602283/up-navigation-broken-on-jellybean">Up navigation broken on JellyBean?</a> */
	@Override public void supportNavigateUpTo(Intent upIntent) {
		upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(upIntent);
		finish();
	}
}
