package net.twisterrob.inventory.android.activity;

import java.lang.annotation.*;
import java.util.*;

import org.slf4j.*;

import android.content.Context;
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

import com.android.debug.hv.ViewServer;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.activity.dev.DeveloperActivity;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.IconedItem.IntentLauncher;

import static net.twisterrob.inventory.android.Constants.Dimensions.*;
import static net.twisterrob.inventory.android.activity.BaseActivity.For.Feature.*;

public class BaseActivity extends ActionBarActivity {
	@For(Log) private static final Logger LOG = LoggerFactory.getLogger(BaseActivity.class);
	@For(Drawer) private ActionBarDrawerToggle mDrawerToggle;
	@For(Drawer) private DrawerLayout mDrawerLayout;

	@For({PixelView, Log}) @Override protected void onCreate(Bundle savedInstanceState) {
		LOG.trace("Creating {}@{}\n{} {}",
				getClass().getSimpleName(),
				Integer.toHexString(System.identityHashCode(this)),
				getIntent(),
				AndroidTools.toString(getIntent().getExtras())
		);
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) {
			ViewServer.get(this).addWindow(this);
		}
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		AndroidTools.showActionBarOverflowIcons(featureId, menu, true);
		return super.onMenuOpened(featureId, menu);
	}

	@For(Drawer) @Override public void onSupportContentChanged() {
		super.onSupportContentChanged();

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
		if (mDrawerLayout != null) {
			mDrawerToggle = new StandardMyActionBarDrawerToggle(this, mDrawerLayout);
			mDrawerLayout.setDrawerListener(mDrawerToggle);

			initDrawers();
		}
	}

	private void initDrawers() {
		ListView drawerLeft = (ListView)mDrawerLayout.findViewById(R.id.drawer_left_list);
		drawerLeft.setAdapter(new IconedItemAdapter(this, R.layout.item_drawer_left, createActions()));
		drawerLeft.setOnItemClickListener(new IntentLauncher(this) {
			@Override public void onItemClick(AdapterView parent, View view, int position, long id) {
				super.onItemClick(parent, view, position, id);
				mDrawerLayout.closeDrawer(parent);
			}
		});
	}

	@For(Drawer) protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (hasDrawer()) {
			mDrawerToggle.syncState(); // Sync the toggle state after onRestoreInstanceState has occurred.
		}
	}

	@For(PixelView) @Override protected void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG) {
			ViewServer.get(this).setFocusedWindow(this);
		}
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
		super.onBackPressed();
	}

	@For(Drawer) @Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (hasDrawer()) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@For(PixelView) @Override protected void onDestroy() {
		if (BuildConfig.DEBUG) {
			ViewServer.get(this).removeWindow(this);
		}
		super.onDestroy();
	}

	@For(Drawer) public static Collection<IconedItem> createActions() {
		Collection<IconedItem> acts = new ArrayList<>();

		acts.add(new SVGIconItem(R.string.home_title, R.raw.property_home, MainActivity.home()));
		acts.add(new SVGIconItem(R.string.sunburst_title, R.raw.ic_sunburst, SunburstActivity.showAll()));
		acts.add(new SVGIconItem(R.string.property_list, R.raw.property_unknown, PropertyListActivity.listAll()));
		acts.add(new SVGIconItem(R.string.category_list, R.raw.category_unknown, CategoryViewActivity.listAll()));
		acts.add(new SVGIconItem(R.string.room_list, R.raw.room_unknown, PropertyViewActivity.listAll()));
		acts.add(new SVGIconItem(R.string.item_list, R.raw.category_box, CategoryItemsActivity.listAll()));

		if (BuildConfig.DEBUG) {
			acts.add(new SVGIconItem(Constants.INVALID_RESOURCE_ID, R.raw.category_chip, DeveloperActivity.show()) {
				@Override public CharSequence getTitle(Context context) {
					return "Developer Tools";
				}
			});
		}
		return acts;
	}

	@For(Drawer) protected boolean hasDrawer() {
		return mDrawerLayout != null && mDrawerToggle != null;
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
		Drawable svg = App.pic().getSVG(this, resourceId, getActionbarIconSize(this), getActionbarIconPadding(this));
		setIcon(svg);
	}

	@For(Drawer) private static class StandardMyActionBarDrawerToggle extends ActionBarDrawerToggle {
		private final ActionBarActivity activity;

		public StandardMyActionBarDrawerToggle(ActionBarActivity activity, DrawerLayout drawerLayout) {
			super(activity, drawerLayout, Constants.INVALID_RESOURCE_ID, Constants.INVALID_RESOURCE_ID);
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
			activity.getSupportActionBar().setTitle(activity.getText(R.string.app_name));
			activity.supportInvalidateOptionsMenu();
		}
	}

	@Retention(RetentionPolicy.SOURCE) @interface For {
		Feature[] value();
		enum Feature {
			Log,
			Children,
			PixelView,
			Drawer,
		}
	}
}
