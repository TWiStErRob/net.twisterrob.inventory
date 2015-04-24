package net.twisterrob.inventory.android.activity;

import java.util.*;

import org.slf4j.*;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.*;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.view.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.CategoryContentsFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.data.PropertyListFragment.PropertiesEvents;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;
import net.twisterrob.inventory.android.fragment.data.SunburstFragment.SunBurstEvents;

public class MainActivity extends BaseActivity
		implements PropertiesEvents, RoomsEvents, CategoriesEvents, SunBurstEvents {
	private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

	public static final String EXTRA_PAGE = "page";
	public static final String PAGE_EMPTY = "empty";
	public static final String PAGE_HOME = "home";
	public static final String PAGE_PROPERTIES = "properties";
	public static final String PAGE_ROOMS = "rooms";
	public static final String PAGE_CATEGORIES = "categories";
	public static final String PAGE_ITEMS = "items";
	public static final String PAGE_SUNBURST = "sunburst";

	private static final Map<String, Integer> TITLES = new HashMap<String, Integer>() {
		{
			put(PAGE_EMPTY, R.string.empty);
			put(PAGE_HOME, R.string.home_title);
			put(PAGE_CATEGORIES, R.string.category_list);
			put(PAGE_PROPERTIES, R.string.property_list);
			put(PAGE_ROOMS, R.string.room_list);
			put(PAGE_ITEMS, R.string.item_list);
			put(PAGE_SUNBURST, R.string.sunburst_title);
		}
	};

	private BaseFragment getFragment() {
		return (BaseFragment)getSupportFragmentManager().findFragmentById(R.id.activityRoot);
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setIcon(getResources().getDrawable(R.drawable.ic_launcher));
		setContentView(R.layout.generic_activity_drawer);

		if (savedInstanceState == null) {
			handleIntent(getIntent());

			if (Constants.DISABLE) {
				onOptionsItemSelected(new MenuBuilder(this).add(0, R.id.debug, 0, "Debug"));
			}
		}

		getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
			@Override public void onBackStackChanged() {
				FragmentManager fm = getSupportFragmentManager();
				int count = fm.getBackStackEntryCount();
				//AndroidTools.dumpBackStack(getApplicationContext(), fm);
				if (count == 0) {
					finish();
				} else {
					BaseFragment fragment = getFragment();
					refreshDrawers((Intent)fragment.getViewTag());

					BackStackEntry top = fm.getBackStackEntryAt(count - 1);
					CharSequence title = getString(TITLES.get(top.getName()));
					setActionBarTitle(title); // to display now
					setActionBarSubtitle(null); // clear to change
					setTitle(title); // to persist and display later (e.g. onDrawerClosed)
				}
			}
		});
	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		String page = getExtraPage(intent);

		FragmentManager m = getSupportFragmentManager();
		int count = m.getBackStackEntryCount();

		if (0 < count) {
			if (page.equals(PAGE_HOME) && page.equals(m.getBackStackEntryAt(0).getName())) {
				LOG.trace("Fragment '{}' already exists on the bottom of the stack, skipping the new request", page);
				return;
			}
			BackStackEntry top = m.getBackStackEntryAt(count - 1);
			if (page.equals(top.getName())) {
				LOG.trace("Fragment '{}' is already on top, skipping the new request", page);
				return;
			}
		}

		if (1 < count) {
			BackStackEntry topCandidate = m.getBackStackEntryAt(count - 2);
			if (page.equals(topCandidate.getName())) {
				LOG.trace("There's already a '{}' behind the top '{}', simulating back navigation",
						page, m.getBackStackEntryAt(count - 1).getName());
				m.popBackStack();
				return;
			}
		}

		BaseFragment fragment = createPage(page);
		fragment.setViewTag(intent);
		m
				.beginTransaction()
				.replace(R.id.activityRoot, fragment)
				.addToBackStack(page)
				.commit()
		;
	}
	private BaseFragment createPage(String page) {
		BaseFragment fragment;
		switch (page) {
			case PAGE_EMPTY:
				fragment = new BaseFragment();
				break;
			case PAGE_CATEGORIES:
				fragment = CategoryContentsFragment.newInstance(null, false);
				break;
			case PAGE_PROPERTIES:
				fragment = PropertyListFragment.newInstance();
				break;
			case PAGE_ROOMS:
				fragment = RoomListFragment.newInstance(Room.ID_ADD);
				break;
			case PAGE_ITEMS:
				fragment = CategoryContentsFragment.newInstance(null, true);
				break;
			case PAGE_HOME:
				fragment = MainFragment.newInstance();
				break;
			case PAGE_SUNBURST:
				fragment = SunburstFragment.newInstance();
				break;
			default:
				throw new IllegalArgumentException("Page is not supported: " + page);
		}
		return fragment;
	}

	private @NonNull String getExtraPage(Intent intent) {
		String page = intent.getStringExtra(EXTRA_PAGE);
		return page != null? page : PAGE_HOME;
	}

	@Override protected void onResume() {
		super.onResume();
		getFragment().refresh();
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		if (!super.onCreateOptionsMenu(menu)) {
			return false;
		}
		getMenuInflater().inflate(R.menu.main, menu);
		getMenuInflater().inflate(R.menu.search, menu);
		AndroidTools.prepareSearch(this, menu, R.id.search);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.debug: {
				Cursor cursor = App.db().listItems();
				cursor.moveToPosition(cursor.getCount() / 2);
				long id = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
				cursor.close();
				Uri uri = InventoryContract.Item.imageUri(id);
				startActivity(ImageActivity.show(uri));
				//startActivity(CategoryActivity.show(7000));
				//startActivity(ItemViewActivity.show(1723));
				//startActivityForResult(CaptureImage.saveTo(this, new File(getCacheDir(), "dev.jpg")), 32767);
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 32767 && resultCode == RESULT_OK) {
			startActivity(ImageActivity.show(data.getData()));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void categorySelected(long id) {
		startActivity(CategoryActivity.show(id));
	}
	public void categoryActioned(long id) {
		startActivity(CategoryActivity.show(id));
	}

	public void newItem(long parentID) {
		throw new UnsupportedOperationException("newItem shouldn't be called");
	}
	public void itemSelected(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}
	public void itemActioned(long itemID) {
		startActivity(ItemEditActivity.edit(itemID));
	}

	public void newProperty() {
		startActivity(PropertyEditActivity.add());
	}
	public void propertySelected(long id) {
		startActivity(PropertyViewActivity.show(id));
	}
	public void propertyActioned(long id) {
		startActivity(PropertyEditActivity.edit(id));
	}

	public void newRoom(long propertyID) {
		startActivity(RoomEditActivity.add(propertyID));
	}
	public void roomSelected(long roomID) {
		startActivity(RoomViewActivity.show(roomID));
	}
	public void roomActioned(long roomID) {
		startActivity(RoomEditActivity.edit(roomID));
	}

	public void rootChanged(SunburstFragment.Node root) {
		setActionBarSubtitle(root.getLabel());
	}

	public static Intent home() {
		Intent intent = new Intent(App.getAppContext(), MainActivity.class);
		return intent;
	}

	public static Intent list(String page) {
		Intent intent = new Intent(App.getAppContext(), MainActivity.class);
		intent.putExtra(EXTRA_PAGE, page);
		return intent;
	}
}
