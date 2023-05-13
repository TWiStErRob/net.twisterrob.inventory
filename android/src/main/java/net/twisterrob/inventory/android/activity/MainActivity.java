package net.twisterrob.inventory.android.activity;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.view.*;

import com.bumptech.glide.Glide;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManager.*;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.wiring.CollapseActionViewOnSubmit;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.backup.Importer.*;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.MainFragment.MainEvents;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.CategoryContentsFragment.CategoriesEvents;
import net.twisterrob.inventory.android.fragment.data.PropertyListFragment.PropertiesEvents;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;
import net.twisterrob.inventory.android.sunburst.SunburstFragment;
import net.twisterrob.inventory.android.sunburst.SunburstFragment.SunBurstEvents;
import net.twisterrob.inventory.android.view.DrawerNavigator;
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
	private static final Map<String, Integer> TITLES = createTitles();
	private static Map<String, Integer> createTitles() {
		Map<String, Integer> titles = new HashMap<>();
		titles.put(PAGE_EMPTY, R.string.empty);
		titles.put(PAGE_HOME, R.string.home_title);
		titles.put(PAGE_CATEGORIES, R.string.category_list);
		titles.put(PAGE_CATEGORY_HELP, R.string.category_guide);
		titles.put(PAGE_PROPERTIES, R.string.property_list);
		titles.put(PAGE_ROOMS, R.string.room_list);
		titles.put(PAGE_ITEMS, R.string.item_list);
		titles.put(PAGE_SUNBURST, R.string.sunburst_title);
		return titles;
	}

	public static final int REQUEST_CODE_IMAGE = 32767;

	private BaseFragment<?> getFragment() {
		return getFragment(R.id.activityRoot);
	}

	private boolean isInventoryEmptyCache;
	private Bundle tempSavedViewState;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setIcon(ContextCompat.getDrawable(this, R.drawable.ic_launcher));
		setContentView(R.layout.generic_activity_drawer);

		if (savedInstanceState == null) {
			handleIntent();
			autoDebug();
		} else { // on rotation, or warm start
			this.tempSavedViewState = savedInstanceState.getBundle("android:viewHierarchyState");
			// see onRestoreInstanceState and onRestart
		}

		getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
			@Override public void onBackStackChanged() {
				if (BuildConfig.DEBUG) {
					LOG.trace(StringerTools.toString(getSupportFragmentManager()));
				}
				if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
					finish();
				} else {
					updateUI();
					refresh(); // FIXME why was this working a month ago without this?
				}
			}
		});

		// First will show last, because the new ones open on top of the previous.
		welcome();
	}

	@SuppressLint("RestrictedApi") // MenuBuilder() and add() are used, but only for debugging
	private void autoDebug() {
		if (Constants.DISABLE) {
			onOptionsItemSelected(new MenuBuilder(this)
					.add(0, R.id.debug, 0, "Debug"));
		}
	}

	private void welcome() {
		if (!App.prefs().getBoolean(R.string.pref_showWelcome, R.bool.pref_showWelcome_default)) {
			return;
		}

		@SuppressWarnings("deprecation") Spanned message = Html.fromHtml(getString(R.string.welcome_question));
		new AlertDialog.Builder(this)
				.setTitle(R.string.welcome_title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						App.prefs().setBoolean(R.string.pref_showWelcome, false);
						App.toastUser(getString(R.string.welcome_help_tip));
						new PopulateSampleInventoryTask().execute();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						App.prefs().setBoolean(R.string.pref_showWelcome, false);
						App.toastUser(getString(R.string.welcome_help_tip));
						// NO OP the app is ready to be used
					}
				})
				.setNeutralButton(R.string.welcome_backup, new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						App.prefs().setBoolean(R.string.pref_showWelcome, false);
						startActivity(BackupActivity.chooser(App.getAppContext()));
					}
				})
				.setCancelable(true)
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override public void onCancel(DialogInterface dialog) {
						App.prefs().setBoolean(R.string.pref_showWelcome, true); // just to be explicit
						MainActivity.this.finish();
					}
				})
				.show();
	}

	@Override protected void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// In case MainActivity is being restored after coming back from another activity the drawer will restore its
		// state to when the drawer item was clicked, restore our known good selection to override that behavior.
		updateUI();
	}
	@Override protected void onRestart() {
		super.onRestart();
		// In case MainActivity is being restarted from behind another activity that may have been launched from
		// the drawer. Selecting the drawer item may have checked it, so let's restore our known selection.
		updateUI();
	}

	private void updateUI() {
		setIntent((Intent)getFragment().getViewTag());
		DrawerNavigator.get(mDrawerLeft).select(getIntent());

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

		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0 && BuildConfig.DEBUG) {
			LOG.warn("Possible up-navigation: {}", StringerTools.toString(getIntent()));
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
		search.setOnQueryTextListener(new CollapseActionViewOnSubmit(getSupportActionBar()));
		if (tempSavedViewState != null) {
			// dirty and ugly hack to make these tests pass:
			// net.twisterrob.inventory.android.activity.MainActivityTest_Search.testRotate()
			// net.twisterrob.inventory.android.activity.MainActivityTest_Search.testSelectClose()
			// TODO figure out a better way or report a compat bug
//			View actionBar = findViewById(R.id.action_bar);
//			actionBar.restoreHierarchyState(tempSavedViewState.getSparseParcelableArray("android:views"));
//			View focused = actionBar.findViewById(tempSavedViewState.getInt("android:focusedViewId", 0));
//			if (focused != null) {
//				focused.requestFocus();
//			}
			tempSavedViewState = null;
		}
		return true;
	}

	private boolean shouldCreateOptionsMenu(Menu menu) {
		BaseFragment<?> fragment = getFragment();
		if (fragment == null) {
			return super.onCreateOptionsMenu(menu);
		}
		// save current state to arguments because it will be preserved on rotation
		Bundle args = fragment.requireArguments();
		if (args.containsKey(OPTIONS_MENU_BACKUP)) {
			fragment.setHasOptionsMenu(args.getBoolean(OPTIONS_MENU_BACKUP));
			args.remove(OPTIONS_MENU_BACKUP);
		}
		if (!super.onCreateOptionsMenu(menu)) {
			args.putBoolean(OPTIONS_MENU_BACKUP, fragment.hasSetOptionsMenu());
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
		ViewTools.visibleIf(menu, R.id.action_demo_inventory, isInventoryEmptyCache);
		return result;
	}

	@SuppressLint("WrongThread")
	@DebugHelper
	public boolean onDebugOptionsItemSelected(MenuItem item) {
		if (BuildConfig.DEBUG) {
			switch (item.getItemId()) {
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
					File devFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "dev.jpg");
					Uri target = Uri.fromFile(devFile);
					startActivityForResult(CaptureImage.saveTo(this, devFile, target, 8192), REQUEST_CODE_IMAGE);
					return true;
				case R.id.debug_testdb:
					resetToTestDatabase();
					return true;
				default:
					return false;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("StaticFieldLeak")
	@DebugHelper
	private void resetToTestDatabase() {
		new android.os.AsyncTask<Void, Void, Void>() {
			@Override protected void onPreExecute() {
				Glide.get(getApplicationContext()).clearMemory();
			}
			@Override protected Void doInBackground(Void... params) {
				Glide.get(getApplicationContext()).clearDiskCache();
				App.db().resetToTest();
				return null;
			}
			@Override protected void onPostExecute(Void aVoid) {
				refresh();
			}
		}.execute();
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
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

	public static Intent home(Context context) {
		return list(context, PAGE_HOME);
	}

	public static Intent list(Context context, String page) {
		Intent intent = new Intent(context, MainActivity.class);
		if (!PAGE_HOME.equals(page)) {
			// Don't include EXTRA_PAGE for home() to have the navigation drawer match with external launch Intent
			intent.putExtra(EXTRA_PAGE, page);
		}
		return intent;
	}

	@SuppressLint("WrongThreadInterprocedural")
	private static Intent improveCategories(@NonNull Context context, @NonNull String email, @Nullable Long categoryId) {
		String subject = context.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME + " Category Feedback";
		Intent intent = new Intent(Intent.ACTION_VIEW)
				.setData(Uri.parse("mailto:"))
				.putExtra(Intent.EXTRA_EMAIL, new String[] {email})
				.putExtra(Intent.EXTRA_SUBJECT, subject);
		String text = "How can we improve the Categories?";
		if (categoryId != null) {
			String categoryKey = CategoryDTO.getCache(context).getCategoryKey(categoryId);
			text += "\n(Suggestion was triggered in context of category: " + categoryKey + ")";
		}
		intent.putExtra(Intent.EXTRA_TEXT, text);
		return intent;
	}
	public static void startImproveCategories(@NonNull Context context, @Nullable Long categoryId) {
		String email = "feedback@twisterrob.net";
		try {
			context.startActivity(improveCategories(context, email, categoryId));
		} catch (ActivityNotFoundException ex) {
			LOG.warn("Cannot start feedback intent", ex);
			App.toastUser(context.getString(R.string.about_feedback_fail, email));
		}
	}

	@SuppressWarnings("deprecation")
	private class RefreshInventorySizeTask extends android.os.AsyncTask<Void, Void, Boolean> {

		public void execute() {
			// Overridden to hide deprecation warnings at call-site.
			super.execute();
		}

		@Override protected void onPreExecute() {
			isInventoryEmptyCache = false;
			supportInvalidateOptionsMenu();
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
			supportInvalidateOptionsMenu(); // make the populate icon appear/disappear (may hide overflow altogether)
		}
	}

	@SuppressWarnings("deprecation")
	private class PopulateSampleInventoryTask extends android.os.AsyncTask<Void, Void, Boolean> {

		public void execute() {
			// Overridden to hide deprecation warnings at call-site.
			super.execute();
		}

		@Override protected void onPreExecute() {
			isInventoryEmptyCache = false;
			supportInvalidateOptionsMenu();
		}
		@Override protected Boolean doInBackground(Void... params) {
			InputStream demo = null;
			try {
				demo = getAssets().open("demo.xml");
				new XMLImporter(getResources(), App.db(), new Types(App.db())).doImport(demo, new ImportProgress() {
					@Override public void publishStart(int size) {
						// NO OP
					}
					@Override public void publishIncrement() {
						// NO OP
					}
					@Override public void warning(String message) {
						throw new UnsupportedOperationException();
					}
					@Override public void error(String message) {
						throw new UnsupportedOperationException();
					}
				}, new ImportImageGetter() {
					@Override public void importImage(Type type, long id, String name, String image) {
						throw new UnsupportedOperationException();
					}
				});
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
