package net.twisterrob.inventory.android.activity;

import java.util.*;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.*;
import android.widget.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.activity.dev.DeveloperActivity;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.view.*;

import static net.twisterrob.inventory.android.content.contract.Category.*;

public class BaseDrawerActivity extends BaseActivity {
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected Collection<IconedItem> createActions() {
		Collection<IconedItem> acts = new ArrayList<>();

		// TODO add main activity link
		acts.add(new SVGIconItem(R.string.property_list, R.raw.property_unknown, PropertyListActivity.list()));
		acts.add(new SVGIconItem(R.string.category_list, R.raw.category_unknown, CategoryViewActivity.show(INTERNAL)));
		acts.add(new SVGIconItem(R.string.room_all, R.raw.room_unknown, PropertyViewActivity.show(Property.ID_ADD)));
		acts.add(new SVGIconItem(R.string.item_all, R.raw.category_storage_box, CategoryItemsActivity.show(INTERNAL)));
		acts.add(new SVGIconItem(R.string.sunburst_title, R.raw.category_disc, SunBurstActivity.show()));

		if (BuildConfig.DEBUG) {
			acts.add(new SVGIconItem("Developer Tools", R.raw.category_electric, DeveloperActivity.show()));
		}
		return acts;
	}

	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// need to have setContentView called in child, but that conflicts with super.onCreate
		final ListView drawerLeft = (ListView)findViewById(R.id.drawer_left_list);
		drawerLeft.setAdapter(new IconedItemAdapter(this, R.layout.drawer_left_item, createActions()));
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				Constants.INVALID_RESOURCE_ID, Constants.INVALID_RESOURCE_ID) {
			@Override
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(getTitle());
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(R.string.app_name);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		drawerLeft.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				drawerLeft.setItemChecked(position, true);
				mDrawerLayout.closeDrawer(drawerLeft);
				IconedItem item = (IconedItem)parent.getItemAtPosition(position);
				startActivity(item.getIntent());
			}
		});
		mDrawerToggle.syncState(); // Sync the toggle state after onRestoreInstanceState has occurred.
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
