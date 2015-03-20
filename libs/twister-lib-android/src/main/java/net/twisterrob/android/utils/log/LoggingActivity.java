package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.util.AttributeSet;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.java.utils.*;

public class LoggingActivity extends ActionBarActivity {
	private static final Logger LOG = LoggerFactory.getLogger("Activity");

	protected LoggingDebugProvider debugInfoProvider;

	public LoggingActivity() {
		log("ctor");
	}

	//region Startup

	@Override public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		log("onCreate", savedInstanceState, persistentState);
		super.onCreate(savedInstanceState, persistentState);
	}
	@Override protected void onCreate(Bundle savedInstanceState) {
		log("onCreate", savedInstanceState);
		super.onCreate(savedInstanceState);
		LoaderManager lm = getSupportLoaderManager();
		LOG.trace("{}.loaderManager={}({})", getName(), lm, ReflectionTools.get(lm, "mWho"));
	}

	// @Override public void onContentChanged() { } // final in super
	@Override public void onSupportContentChanged() {
		log("onSupportContentChanged");
		super.onSupportContentChanged();
	}
	@Override public View onCreateView(View parent, String name, @NonNull Context context,
			@NonNull AttributeSet attrs) {
		//log("onCreateView", parent, name, context, attrs);
		return super.onCreateView(parent, name, context, attrs);
	}
	@Override public View onCreateView(String name, @NonNull Context context, @NonNull AttributeSet attrs) {
		//log("onCreateView", name, context, attrs);
		return super.onCreateView(name, context, attrs);
	}

	@Override public void onAttachFragment(android.app.Fragment fragment) {
		log("onAttachFragment", fragment);
		super.onAttachFragment(fragment);
	}
	@Override public void onAttachFragment(Fragment fragment) {
		log("onAttachFragment", fragment);
		super.onAttachFragment(fragment);
	}

	@Override protected void onRestart() {
		log("onRestart");
		super.onRestart();
	}

	@Override protected void onStart() {
		log("onStart");
		super.onStart();
	}

	@Override public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
		log("onRestoreInstanceState", savedInstanceState, persistentState);
		super.onRestoreInstanceState(savedInstanceState, persistentState);
	}
	@Override protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		log("onRestoreInstanceState", savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		log("onPostCreate", savedInstanceState, persistentState);
		super.onPostCreate(savedInstanceState, persistentState);
	}
	@Override protected void onPostCreate(Bundle savedInstanceState) {
		log("onPostCreate", savedInstanceState);
		super.onPostCreate(savedInstanceState);
	}

	@Override protected void onResume() {
		log("onResume");
		super.onResume();
	}
	@Override protected void onResumeFragments() {
		log("onResumeFragments");
		super.onResumeFragments();
	}
	@Override protected void onPostResume() {
		log("onPostResume");
		super.onPostResume();
	}

	@Override public void onAttachedToWindow() {
		log("onAttachedToWindow");
		super.onAttachedToWindow();
	}

	@Override public void onUserInteraction() {
		log("onUserInteraction");
		super.onUserInteraction();
	}

	//endregion Startup

	// activity is running

	//region Shutdown

	@Override protected void onUserLeaveHint() {
		log("onUserLeaveHint");
		super.onUserLeaveHint();
	}

	@Override protected void onPause() {
		log("onPause");
		super.onPause();
	}

	@Override public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		log("onSaveInstanceState", outState, outPersistentState);
		super.onSaveInstanceState(outState, outPersistentState);
	}
	@Override protected void onSaveInstanceState(Bundle outState) {
		log("onSaveInstanceState", outState);
		super.onSaveInstanceState(outState);
	}

	@Override protected void onStop() {
		log("onStop");
		super.onStop();
	}

	@Override public void onDetachedFromWindow() {
		log("onDetachedFromWindow");
		super.onDetachedFromWindow();
	}

	@Override protected void onDestroy() {
		log("onDestroy");
		super.onDestroy();
	}

	//endregion

	//region OptionsMenu

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		log("onCreateOptionsMenu", menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		log("onPrepareOptionsMenu", menu);
		return super.onPrepareOptionsMenu(menu);
	}
	@Override public void openOptionsMenu() {
		log("openOptionsMenu");
		super.openOptionsMenu();
	}
	@Override public boolean onMenuOpened(int featureId, Menu menu) {
		log("onMenuOpened", AndroidTools.toFeatureString(featureId), menu);
		return super.onMenuOpened(featureId, menu);
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		log("onOptionsItemSelected", item);
		return super.onOptionsItemSelected(item);
	}
	@Override public void closeOptionsMenu() {
		log("closeOptionsMenu");
		super.closeOptionsMenu();
	}
	@Override public void onOptionsMenuClosed(Menu menu) {
		log("onOptionsMenuClosed", menu);
		super.onOptionsMenuClosed(menu);
	}
	@Override public void invalidateOptionsMenu() {
		log("invalidateOptionsMenu");
		super.invalidateOptionsMenu();
	}
	@Override public void supportInvalidateOptionsMenu() {
		log("supportInvalidateOptionsMenu");
		super.supportInvalidateOptionsMenu();
	}

	//endregion OptionsMenu

	//region ActionMode

	@Override public void onSupportActionModeStarted(ActionMode mode) {
		log("onSupportActionModeStarted", mode);
		super.onSupportActionModeStarted(mode);
	}
	@Override public void onSupportActionModeFinished(ActionMode mode) {
		log("onSupportActionModeFinished", mode);
		super.onSupportActionModeFinished(mode);
	}
	@Override public ActionMode startSupportActionMode(Callback callback) {
		log("startSupportActionMode", callback);
		return super.startSupportActionMode(callback);
	}
	@Nullable @Override public android.view.ActionMode onWindowStartingActionMode(
			android.view.ActionMode.Callback callback) {
		log("onWindowStartingActionMode", callback);
		return super.onWindowStartingActionMode(callback);
	}
	@Override public void onActionModeStarted(android.view.ActionMode mode) {
		log("onActionModeStarted", mode);
		super.onActionModeStarted(mode);
	}
	@Override public void onActionModeFinished(android.view.ActionMode mode) {
		log("onActionModeFinished", mode);
		super.onActionModeFinished(mode);
	}

	//endregion ActionMode

	//region OptionsPanel

	@Override public View onCreatePanelView(int featureId) {
		log("onCreatePanelView", AndroidTools.toFeatureString(featureId));
		return super.onCreatePanelView(featureId);
	}
	@Override public boolean onCreatePanelMenu(int featureId, Menu menu) {
		log("onCreatePanelMenu", AndroidTools.toFeatureString(featureId), menu);
		return super.onCreatePanelMenu(featureId, menu);
	}
	@Override public boolean onPreparePanel(int featureId, View view, Menu menu) {
		log("onPreparePanel", AndroidTools.toFeatureString(featureId), view, menu);
		return super.onPreparePanel(featureId, view, menu);
	}
	@Override protected boolean onPrepareOptionsPanel(View view, Menu menu) {
		log("onPrepareOptionsPanel", view, menu);
		return super.onPrepareOptionsPanel(view, menu);
	}
	@Override public void onPanelClosed(int featureId, Menu menu) {
		log("onPanelClosed", AndroidTools.toFeatureString(featureId), menu);
		super.onPanelClosed(featureId, menu);
	}

	//endregion OptionsPanel

	//region Up navigation

	@Override public boolean onNavigateUp() {
		log("onNavigateUp");
		return super.onNavigateUp();
	}
	@Override public boolean onSupportNavigateUp() {
		log("onSupportNavigateUp");
		return super.onSupportNavigateUp();
	}
	@Override public boolean onNavigateUpFromChild(Activity child) {
		log("onNavigateUpFromChild", child);
		return super.onNavigateUpFromChild(child);
	}

	@Override public void onCreateNavigateUpTaskStack(@NonNull android.app.TaskStackBuilder builder) {
		log("onCreateNavigateUpTaskStack", builder);
		super.onCreateNavigateUpTaskStack(builder);
	}
	@Override public void onCreateSupportNavigateUpTaskStack(TaskStackBuilder builder) {
		log("onCreateSupportNavigateUpTaskStack", builder);
		super.onCreateSupportNavigateUpTaskStack(builder);
	}

	@Override public void onPrepareNavigateUpTaskStack(android.app.TaskStackBuilder builder) {
		log("onPrepareNavigateUpTaskStack", builder);
		super.onPrepareNavigateUpTaskStack(builder);
	}
	@Override public void onPrepareSupportNavigateUpTaskStack(TaskStackBuilder builder) {
		log("onPrepareSupportNavigateUpTaskStack", builder);
		super.onPrepareSupportNavigateUpTaskStack(builder);
	}

	//endregion Up navigation

	//region Activity Navigation

	@Override public void onBackPressed() {
		log("onBackPressed");
		super.onBackPressed();
	}
	@Override public boolean onSearchRequested() {
		log("onSearchRequested");
		return super.onSearchRequested();
	}

	@Override public void startActivityForResult(Intent intent, int requestCode) {
		log("startActivityForResult", intent, requestCode);
		super.startActivityForResult(intent, requestCode);
	}
	@Override public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
		log("startActivityFromFragment", fragment, intent, requestCode);
		super.startActivityFromFragment(fragment, intent, requestCode);
	}

	@Override public void onActivityReenter(int resultCode, Intent data) {
		log("onActivityReenter", resultCode, data);
		super.onActivityReenter(resultCode, data);
	}
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		log("onActivityResult", requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override protected void onNewIntent(Intent intent) {
		log("onNewIntent", intent);
		super.onNewIntent(intent);
	}
	@Override public Object onRetainCustomNonConfigurationInstance() {
		log("onRetainCustomNonConfigurationInstance");
		return super.onRetainCustomNonConfigurationInstance();
	}
	@Override public void onConfigurationChanged(Configuration newConfig) {
		log("onConfigurationChanged", newConfig, getResources().getConfiguration());
		super.onConfigurationChanged(newConfig);
	}

	//endregion Activity Navigation

	//region Context Menu
	@Override public void registerForContextMenu(@NonNull View view) {
		log("registerForContextMenu", view);
		super.registerForContextMenu(view);
	}
	@Override public void openContextMenu(@NonNull View view) {
		log("openContextMenu", view);
		super.openContextMenu(view);
	}

	@Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		log("onCreateContextMenu", menu, v, menuInfo);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override public boolean onContextItemSelected(MenuItem item) {
		log("onContextItemSelected", item);
		return super.onContextItemSelected(item);
	}

	@Override public void closeContextMenu() {
		log("closeContextMenu");
		super.closeContextMenu();
	}
	@Override public void onContextMenuClosed(Menu menu) {
		log("onContextMenuClosed", menu);
		super.onContextMenuClosed(menu);
	}

	@Override public void unregisterForContextMenu(@NonNull View view) {
		log("unregisterForContextMenu", view);
		super.unregisterForContextMenu(view);
	}

	//endregion

	@Override public void onLowMemory() {
		log("onLowMemory");
		super.onLowMemory();
	}
	@Override public void onTrimMemory(int level) {
		log("onTrimMemory", AndroidTools.toTrimMemoryString(level));
		super.onTrimMemory(level);
	}

	@Override protected void onTitleChanged(CharSequence title, int color) {
		log("onTitleChanged", title, AndroidTools.toColorString(color));
		super.onTitleChanged(title, color);
	}
	@Override protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
		log("onChildTitleChanged", childActivity, title);
		super.onChildTitleChanged(childActivity, title);
	}

	private void log(String name, Object... args) {
		LoggingHelper.log(LOG, getName(), name, debugInfoProvider, args);
	}

	private String getName() {
		return getClass().getSimpleName() + "@" + StringTools.hashString(this);
	}
}
