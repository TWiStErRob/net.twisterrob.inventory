package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class LoggingFragment extends Fragment {
	private static final Logger LOG = LoggerFactory.getLogger("Fragment");

	protected LoggingDebugProvider debugInfoProvider;

	public LoggingFragment() {
		log("ctor");
	}

	@Override public void onInflate(
			@NonNull Context context,
			@NonNull AttributeSet attrs,
			@Nullable Bundle savedInstanceState
	) {
		log("onInflate", context, attrs, savedInstanceState);
		super.onInflate(context, attrs, savedInstanceState);
	}

	@SuppressWarnings("deprecation")
	@Override public void onInflate(
			@NonNull Activity activity,
			@NonNull AttributeSet attrs,
			@Nullable Bundle savedInstanceState
	) {
		log("onInflate", activity, attrs, savedInstanceState);
		super.onInflate(activity, attrs, savedInstanceState);
	}

	@Override public void setArguments(@Nullable Bundle args) {
		log("setArguments", args);
		super.setArguments(args);
	}

	@Override public void setInitialSavedState(SavedState state) {
		log("setInitialSavedState", state);
		super.setInitialSavedState(state);
	}

	@Override public void onAttach(@NonNull Context context) {
		log("onAttach", context);
		super.onAttach(context);
	}

	@SuppressWarnings("deprecation")
	@Override public void onAttach(@NonNull Activity activity) {
		log("onAttach", activity);
		super.onAttach(activity);
	}

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		log("onCreate", savedInstanceState);
		super.onCreate(savedInstanceState);
	}

	@Override public @Nullable View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState
	) {
		log("onCreateView", inflater, container, savedInstanceState);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		log("onViewCreated", view, savedInstanceState);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		log("onActivityCreated", savedInstanceState);
		super.onActivityCreated(savedInstanceState);
		LOG.trace("{}.loaderManager={}", getName(), LoaderManager.getInstance(this));
	}

	@Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		log("onViewStateRestored", savedInstanceState);
		super.onViewStateRestored(savedInstanceState);
	}

	@Override public void onStart() {
		log("onStart");
		super.onStart();
	}

	// Activity.onPostCreate
	// Activity.onResume/onPostResume/onResumeFragments

	@Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

	@Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
		log("onCreateOptionsMenu", menu, menuInflater);
		super.onCreateOptionsMenu(menu, menuInflater);
	}

	// Activity.onPrepareOptionsMenu

	@Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
		log("onPrepareOptionsMenu", menu);
		super.onPrepareOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		log("onOptionsItemSelected", item);
		return super.onOptionsItemSelected(item);
	}

	@Override public void onOptionsMenuClosed(@NonNull Menu menu) {
		log("onOptionsMenuClosed", menu);
		super.onOptionsMenuClosed(menu);
	}

	@Override public void onDestroyOptionsMenu() {
		log("onDestroyOptionsMenu");
		super.onDestroyOptionsMenu();
	}

	@Override public void onCreateContextMenu(
			@NonNull ContextMenu menu,
			@NonNull View view,
			@Nullable ContextMenuInfo menuInfo
	) {
		log("onCreateContextMenu", menu, view, menuInfo);
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override public boolean onContextItemSelected(@NonNull MenuItem item) {
		log("onContextItemSelected", item);
		return super.onContextItemSelected(item);
	}

	@Override public void onConfigurationChanged(@NonNull Configuration newConfig) {
		log("onConfigurationChanged", newConfig);
		super.onConfigurationChanged(newConfig);
	}

	// Activity.onPause

	@Override public void onPause() {
		log("onPause");
		super.onPause();
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState) {
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

	protected void log(@NonNull String name, @NonNull Object... args) {
		LoggingHelper.log(LOG, getName(), name, debugInfoProvider, args);
	}

	protected @NonNull String getName() {
		return StringerTools.toNameString(this);
	}
}
