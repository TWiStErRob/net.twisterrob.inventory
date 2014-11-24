package net.twisterrob.inventory.android.activity;

import java.util.*;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.*;
import android.widget.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.activity.dev.DeveloperActivity;
import net.twisterrob.inventory.android.view.*;

public class BaseDrawerActivity extends BaseActivity {
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected Collection<IconedItem> createActions() {
		Collection<IconedItem> acts = new ArrayList<>();

		acts.add(new SVGIconItem(R.string.home_title, R.raw.property_home, MainActivity.home()));
		acts.add(new SVGIconItem(R.string.sunburst_title, R.raw.ic_sunburst, SunBurstActivity.showAll()));
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

	@Override public void onSupportContentChanged() {
		super.onSupportContentChanged();

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				Constants.INVALID_RESOURCE_ID, Constants.INVALID_RESOURCE_ID) {
			@Override
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(getTitle());
				supportInvalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(R.string.app_name);
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		final ListView drawerLeft = (ListView)findViewById(R.id.drawer_left_list);
		drawerLeft.setAdapter(new IconedItemAdapter(this, R.layout.drawer_left_item, createActions()));
		drawerLeft.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				drawerLeft.setItemChecked(position, true);
				mDrawerLayout.closeDrawer(drawerLeft);
				IconedItem item = (IconedItem)parent.getItemAtPosition(position);
				startActivity(item.getIntent());
			}
		});
	}

	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState(); // Sync the toggle state after onRestoreInstanceState has occurred.
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	protected boolean isDrawerShown() {
		return mDrawerLayout.isDrawerOpen(GravityCompat.START) || mDrawerLayout.isDrawerOpen(GravityCompat.END);
	}

	@Override public void onBackPressed() {
		if (isDrawerShown()) {
			mDrawerLayout.closeDrawers();
			return;
		}
		super.onBackPressed();
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
