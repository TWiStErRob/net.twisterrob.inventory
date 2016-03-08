package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.*;
import android.util.AttributeSet;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.java.exceptions.StackTrace;
import net.twisterrob.java.utils.*;

public class LoggingFragment extends Fragment {
	private static final Logger LOG = LoggerFactory.getLogger("Fragment");

	protected LoggingDebugProvider debugInfoProvider;

	public LoggingFragment() {
		log("ctor");
	}

	@Override public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
		log("onInflate", context, attrs, savedInstanceState);
		super.onInflate(context, attrs, savedInstanceState);
	}

	@SuppressWarnings("deprecation")
	@Override public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
		log("onInflate", activity, attrs, savedInstanceState);
		super.onInflate(activity, attrs, savedInstanceState);
	}

	@Override public void setArguments(Bundle args) {
		log("setArguments", args);
		super.setArguments(args);
	}

	@Override public void setInitialSavedState(SavedState state) {
		log("setInitialSavedState", state);
		super.setInitialSavedState(state);
	}

	@Override public void onAttach(Context context) {
		log("onAttach", context);
		super.onAttach(context);
	}

	@SuppressWarnings("deprecation")
	@Override public void onAttach(Activity activity) {
		log("onAttach", activity);
		super.onAttach(activity);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		log("onCreate", savedInstanceState);
		super.onCreate(savedInstanceState);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		log("onCreateView", inflater, container, savedInstanceState);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override public void onViewCreated(View view, Bundle bundle) {
		log("onViewCreated", view, bundle);
		super.onViewCreated(view, bundle);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		log("onActivityCreated", savedInstanceState);
		super.onActivityCreated(savedInstanceState);
		LoaderManager lm = super.getLoaderManager();
		LOG.trace("{}.loaderManager={}({})", getName(), lm, ReflectionTools.get(lm, "mWho"));
	}

	@Override public LoaderManager getLoaderManager() {
		LoaderManager lm = super.getLoaderManager();
		String who = ReflectionTools.get(lm, "mWho");
		if (who == null || who.contains("null")) {
			// for example executePendingTransactions() in onCreate()
			LOG.warn("{}.getLoaderManager(): invalid loaderManager {}({}), parent not initialized yet",
					getName(), lm, who, new StackTrace());
		}
		return lm;
	}

	@Override public void onViewStateRestored(Bundle savedInstanceState) {
		log("onViewStateRestored", savedInstanceState);
		super.onViewStateRestored(savedInstanceState);
	}

	@Override public void onStart() {
		log("onStart");
		super.onStart();
	}

	// Activity.onPostCreate
	// Activity.onResume/onPostResume/onResumeFragments

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		log("onActivityResult", requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
	@Override public void onResume() {
		log("onResume");
		super.onResume();
	}
	@Override public void startActivity(Intent intent) {
		log("startActivity", intent);
		super.startActivity(intent);
	}
	@Override public void startActivityForResult(Intent intent, int requestCode) {
		log("startActivityForResult", intent, requestCode);
		super.startActivityForResult(intent, requestCode);
	}

	// Activity.onCreateOptionsMenu

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		log("onCreateOptionsMenu", menu, menuInflater);
		super.onCreateOptionsMenu(menu, menuInflater);
	}

	// Activity.onPrepareOptionsMenu

	@Override public void onPrepareOptionsMenu(Menu menu) {
		log("onPrepareOptionsMenu", menu);
		super.onPrepareOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		log("onOptionsItemSelected", item);
		return super.onOptionsItemSelected(item);
	}

	@Override public void onOptionsMenuClosed(Menu menu) {
		log("onOptionsMenuClosed", menu);
		super.onOptionsMenuClosed(menu);
	}

	@Override public void onDestroyOptionsMenu() {
		log("onDestroyOptionsMenu");
		super.onDestroyOptionsMenu();
	}

	@Override public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		log("onCreateContextMenu", menu, view, menuInfo);
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override public boolean onContextItemSelected(MenuItem item) {
		log("onContextItemSelected", item);
		return super.onContextItemSelected(item);
	}

	@Override public void onConfigurationChanged(Configuration newConfig) {
		log("onConfigurationChanged", newConfig);
		super.onConfigurationChanged(newConfig);
	}

	// Activity.onPause

	@Override public void onPause() {
		log("onPause");
		super.onPause();
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		log("onSaveInstanceState", outState);
		super.onSaveInstanceState(outState);
	}

	// Activity.onStop

	@Override public void onStop() {
		log("onStop");
		super.onStop();
	}

	// Activity.onDestroy

	@Override public void onDestroyView() {
		log("onDestroyView");
		super.onDestroyView();
	}

	@Override public void onDestroy() {
		log("onDestroy");
		super.onDestroy();
	}

	@Override public void onDetach() {
		log("onDetach");
		super.onDetach();
	}

	@Override public void onLowMemory() {
		log("onLowMemory");
		super.onLowMemory();
	}

	private void log(String name, Object... args) {
		LoggingHelper.log(LOG, getName(), name, debugInfoProvider, args);
	}

	private String getName() {
		return getClass().getSimpleName() + "@" + StringTools.hashString(this)
				+ "(" + ReflectionTools.get(this, "mWho") + ")";
	}
}
