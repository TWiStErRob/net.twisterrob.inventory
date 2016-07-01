package net.twisterrob.inventory.android.activity;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.*;

import com.bumptech.glide.Glide;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.io.ImportProgressHandler;
import net.twisterrob.inventory.android.content.io.xml.XMLImporter;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.MainFragment.MainEvents;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.CategoryContentsFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.data.PropertyListFragment.PropertiesEvents;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;
import net.twisterrob.inventory.android.sunburst.SunburstFragment;
import net.twisterrob.inventory.android.sunburst.SunburstFragment.SunBurstEvents;
import net.twisterrob.java.annotations.DebugHelper;

public class MainActivity extends DrawerActivity
		implements PropertiesEvents, RoomsEvents, CategoriesEvents, SunBurstEvents, MainEvents {
	private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

	public static final String EXTRA_PAGE = "page";
	public static final String PAGE_EMPTY = "empty";
	public static final String PAGE_HOME = "home";
	public static final String PAGE_PROPERTIES = "properties";
	public static final String PAGE_ROOMS = "rooms";
	public static final String PAGE_CATEGORIES = "categories";
	public static final String PAGE_CATEGORY_HELP = "category-help";
	public static final String PAGE_ITEMS = "items";
	public static final String PAGE_SUNBURST = "sunburst";

	private static final String OPTIONS_MENU_BACKUP = "OptionsMenu_backup";
	private static final Map<String, Integer> TITLES = new HashMap<String, Integer>() {
		{
			put(PAGE_EMPTY, R.string.empty);
			put(PAGE_HOME, R.string.home_title);
			put(PAGE_CATEGORIES, R.string.category_list);
			put(PAGE_CATEGORY_HELP, R.string.category_guide);
			put(PAGE_PROPERTIES, R.string.property_list);
			put(PAGE_ROOMS, R.string.room_list);
			put(PAGE_ITEMS, R.string.item_list);
			put(PAGE_SUNBURST, R.string.sunburst_title);
		}
	};

	private BaseFragment<?> getFragment() {
		return getFragment(R.id.activityRoot);
	}

	private boolean isInventoryEmptyCache;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher));
		setContentView(R.layout.generic_activity_drawer);

		if (savedInstanceState == null) {
			handleIntent();

			if (Constants.DISABLE) {
				onOptionsItemSelected(new MenuBuilder(this).add(0, R.id.debug, 0, "Debug"));
			}
		} else { // on rotation, or warm start
			updateTitle();
		}

		getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
			@Override public void onBackStackChanged() {
				LOG.trace(AndroidTools.toString(getSupportFragmentManager()));
				if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
					finish();
				} else {
					setIntent((Intent)getFragment().getViewTag());
					updateTitle();
					refresh(); // FIXME why was this working a month ago without this?
				}
			}
		});

		if (App.getBPref(R.string.pref_showWelcome, R.bool.pref_showWelcome_default)) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.welcome_title)
					.setMessage(R.string.welcome_question)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							App.setBPref(R.string.pref_showWelcome, false);
							App.toastUser(getString(R.string.welcome_help_tip));
							new PopulateSampleInventoryTask().execute();
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							App.setBPref(R.string.pref_showWelcome, false);
							App.toastUser(getString(R.string.welcome_help_tip));
							// NO OP the app is ready to be used
						}
					})
					.setNeutralButton(R.string.welcome_backup, new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							App.setBPref(R.string.pref_showWelcome, false);
							startActivity(BackupActivity.chooser());
						}
					})
					.setCancelable(true)
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override public void onCancel(DialogInterface dialog) {
							App.setBPref(R.string.pref_showWelcome, true); // just to be explicit
							MainActivity.this.finish();
						}
					})
					.show();
		}
	}
	private void updateTitle() {
		FragmentManager fm = getSupportFragmentManager();
		BackStackEntry top = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
		CharSequence title = getString(TITLES.get(top.getName()));
		setActionBarTitle(title); // to display now
		setActionBarSubtitle(null); // clear to change
		setTitle(title); // to persist and display later (e.g. onDrawerClosed)
	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent();
	}

	private void handleIntent() {
		String page = getExtraPage();

		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0) {
			LOG.trace("Ignored possible up-navigation, use current page and state for: {}", getIntent());
			return;
		}

		FragmentManager m = getSupportFragmentManager();
		int count = m.getBackStackEntryCount();

		if (0 < count) {
			if (page.equals(PAGE_HOME) && page.equals(m.getBackStackEntryAt(0).getName())) {
				LOG.trace("Fragment '{}' already exists on the bottom of the stack, skipping the new request", page);
				m.popBackStack(PAGE_HOME, 0);
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

		BaseFragment<?> fragment = createPage(page);
		fragment.setViewTag(getIntent());
		m
				.beginTransaction()
				.replace(R.id.activityRoot, fragment)
				.addToBackStack(page)
				.commit()
		;
	}
	private BaseFragment<?> createPage(String page) {
		BaseFragment<?> fragment;
		switch (page) {
			case PAGE_EMPTY:
				fragment = new BaseFragment<>();
				break;
			case PAGE_CATEGORIES:
				fragment = CategoryContentsFragment.newInstance(null, false);
				break;
			case PAGE_CATEGORY_HELP:
				fragment = CategoryHelpFragment.newInstance(Intents.getCategory(getIntent().getExtras()));
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

	private @NonNull String getExtraPage() {
		String page = getIntent().getStringExtra(EXTRA_PAGE);
		return page != null? page : PAGE_HOME;
	}

	@Override protected void onResume() {
		super.onResume();
		refresh();
	}
	private void refresh() {
		getFragment().refresh();
		new RefreshInventorySizeTask().execute();
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		if (!shouldCreateOptionsMenu(menu)) {
			return false;
		}
		getMenuInflater().inflate(R.menu.main, menu);
		getMenuInflater().inflate(R.menu.search, menu);
		SearchView search = (SearchView)AndroidTools.prepareSearch(this, menu, R.id.search);
		search.setQueryRefinementEnabled(true);
		search.setOnQueryTextListener(new OnQueryTextListener() {
			@Override public boolean onQueryTextSubmit(String query) {
				getSupportActionBar().collapseActionView();
				return false;
			}
			@Override public boolean onQueryTextChange(String query) {
				return false;
			}
		});
		return true;
	}
	private boolean shouldCreateOptionsMenu(Menu menu) {
		BaseFragment<?> fragment = getFragment();
		if (fragment == null) {
			return super.onCreateOptionsMenu(menu);
		}
		// save current state to arguments because it will be preserved on rotation
		Bundle args = fragment.getArguments();
		if (args.containsKey(OPTIONS_MENU_BACKUP)) {
			fragment.setHasOptionsMenu(args.getBoolean(OPTIONS_MENU_BACKUP));
			args.remove(OPTIONS_MENU_BACKUP);
		}
		if (!super.onCreateOptionsMenu(menu)) {
			args.putBoolean(OPTIONS_MENU_BACKUP, fragment.hasOptionsMenu());
			fragment.setHasOptionsMenu(false);
			return false;
		}
		return true;
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == R.id.debug && onDebugOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
			case R.id.action_demo_inventory:
				new PopulateSampleInventoryTask().execute();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		MenuItem item = menu.findItem(R.id.action_demo_inventory);
		item.setVisible(isInventoryEmptyCache);
		return result;
	}

	@DebugHelper
	@SuppressLint("all")
	@SuppressWarnings("all")
	public boolean onDebugOptionsItemSelected(MenuItem item) {
		if (BuildConfig.DEBUG) {
			switch (item.getItemId()) {
				case R.id.debug_exportCategories:
					try {
						File file = new File(Paths.getPhoneHome(), "categories.html");
						new CategoryHelpBuilder(this).export(file);
					} catch (Exception ex) {
						LOG.error("Cannot export categories", ex);
					}
					return true;
				case R.id.debug_showImage:
					startActivity(ImageActivity.show(InventoryContract.Item.imageUri(1L)));
					return true;
				case R.id.debug_showCategory:
					startActivity(CategoryActivity.show(7000L));
					return true;
				case R.id.debug_showItem:
					startActivity(ItemViewActivity.show(10010L));
					return true;
				case R.id.debug_capture:
					File devFile = new File("/sdcard/dev.jpg");
					startActivityForResult(CaptureImage.saveTo(this, devFile, Uri.fromFile(devFile), 8192), 32767);
					return true;
				case R.id.debug_testdb:
					new AsyncTask<Void, Void, Void>() {
						@Override protected void onPreExecute() {
							Glide.get(getApplicationContext()).clearMemory();
						}
						@Override protected Void doInBackground(Void... params) {
							Glide.get(getApplicationContext()).clearDiskCache();
							Database.resetToTest();
							return null;
						}
						@Override protected void onPostExecute(Void aVoid) {
							refresh();
						}
					}.execute();
					return true;
				default:
					return false;
			}
		}
		return false;
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 32767 && resultCode == RESULT_OK) {
			startActivity(ImageActivity.show(data.getData()));
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void categorySelected(long id) {
		startActivity(Intents.childNav(CategoryActivity.show(id)));
	}
	public void categoryActioned(long id) {
		startActivity(Intents.childNav(CategoryActivity.show(id)));
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
		startActivity(Intents.childNav(PropertyViewActivity.show(id)));
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

	public void rootChanged(String name) {
		setActionBarSubtitle(name);
	}

	public static Intent home() {
		return list(PAGE_HOME);
	}

	public static Intent list(String page) {
		Intent intent = new Intent(App.getAppContext(), MainActivity.class);
		if (!PAGE_HOME.equals(page)) {
			// Don't include EXTRA_PAGE for home() to have the navigation drawer match with external launch Intent
			intent.putExtra(EXTRA_PAGE, page);
		}
		return intent;
	}
	public static Intent improveCategories(Context context, Long categoryId) {
		Intent intent = new Intent(Intent.ACTION_VIEW)
				.setData(Uri.parse("mailto:" + BuildConfig.EMAIL))
				.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) + " Category Feedback");
		String text = "How can we improve the Categories?";
		if (categoryId != null) {
			String categoryKey = CategoryDTO.getCache(context).getCategoryKey(categoryId);
			text += "\n(Suggestion was triggered in context of category: " + categoryKey + ")";
		}
		intent.putExtra(Intent.EXTRA_TEXT, text);
		return intent;
	}

	private class RefreshInventorySizeTask extends AsyncTask<Void, Void, Boolean> {
		@Override protected void onPreExecute() {
			isInventoryEmptyCache = false;
			invalidateOptionsMenu();
		}
		@SuppressWarnings("TryFinallyCanBeTryWithResources")
		@Override protected Boolean doInBackground(Void... params) {
			try {
				return App.db().isEmpty();
			} catch (Exception ex) {
				LOG.error("Cannot get property list to check empty inventory", ex);
				return null;
			}
		}
		@Override protected void onPostExecute(Boolean result) {
			isInventoryEmptyCache = Boolean.TRUE.equals(result);
			invalidateOptionsMenu(); // make the populate icon appear/disappear (may hide overflow altogether)
		}
	}

	private class PopulateSampleInventoryTask extends AsyncTask<Void, Void, Boolean> {
		@Override protected void onPreExecute() {
			isInventoryEmptyCache = false;
			invalidateOptionsMenu();
		}
		@Override protected Boolean doInBackground(Void... params) {
			InputStream demo = null;
			try {
				demo = getAssets().open("demo.xml");
				new XMLImporter(new ImportProgressHandler() {
					@Override public void publishStart(long size) {
						// NO OP
					}
					@Override public void publishIncrement() {
						// NO OP
					}
					@Override public void warning(@StringRes int stringID, Object... args) {
						throw new UnsupportedOperationException();
					}
					@Override public void error(String message) {
						throw new UnsupportedOperationException();
					}
					@Override public void importImage(Type type, long id, String name, String image)
							throws IOException {
						throw new UnsupportedOperationException();
					}
				}, App.db()).doImport(demo);
				return true;
			} catch (Exception ex) {
				LOG.error("Cannot populate demo sample", ex);
				return null;
			} finally {
				IOTools.ignorantClose(demo);
			}
		}
		@Override protected void onPostExecute(Boolean aBoolean) {
			if (!Boolean.TRUE.equals(aBoolean)) {
				App.toastUser("Cannot populate demo sample, sorry.");
			}
			refresh();
		}
	}
}
