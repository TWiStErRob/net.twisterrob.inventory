package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;

import net.twisterrob.android.utils.tools.AndroidTools;

public class LoggingFragment extends Fragment {
	private static final Logger LOG = LoggerFactory.getLogger("Fragment");
	public LoggingFragment() {
		LOG.trace("{}.ctor", getName());
	}

	@Override
	public void onAttach(Activity activity) {
		LOG.trace("{}.onAttach({})", getName(), activity);
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		LOG.trace("{}.onCreate({})", getName(), AndroidTools.toString(savedInstanceState));
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LOG.trace("{}.onCreateView({}, {}, {})", getName(), inflater, container,
				AndroidTools.toString(savedInstanceState));
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		LOG.trace("{}.onViewCreated({}, {})", getName(), view, AndroidTools.toString(bundle));
		super.onViewCreated(view, bundle);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		LOG.trace("{}.onActivityCreated({})", getName(), AndroidTools.toString(savedInstanceState));
		super.onActivityCreated(savedInstanceState);
		LOG.trace("{}.getLoaderManager(): {}", getName(), LoggingHelper.toString(getLoaderManager()));
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		LOG.trace("{}.onViewStateRestored({})", getName(), AndroidTools.toString(savedInstanceState));
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public void onStart() {
		LOG.trace("{}.onStart", getName());
		super.onStart();
	}

	// Activity.onPostCreate
	// Activity.onResume/onPostResume/onResumeFragments

	@Override
	public void onResume() {
		LOG.trace("{}.onResume", getName());
		super.onResume();
	}

	// Activity.onCreateOptionsMenu

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		LOG.trace("{}.onCreateOptionsMenu({}, {})", getName(), menu, menuInflater);
		super.onCreateOptionsMenu(menu, menuInflater);
	}

	// Activity.onPrepareOptionsMenu

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		LOG.trace("{}.onPrepareOptionsMenu({})", getName(), menu);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LOG.trace("{}.onOptionsItemSelected({})", getName(), item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		LOG.trace("{}.onOptionsMenuClosed({})", getName(), menu);
		super.onOptionsMenuClosed(menu);
	}

	@Override
	public void onDestroyOptionsMenu() {
		LOG.trace("{}.onDestroyOptionsMenu", getName());
		super.onDestroyOptionsMenu();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		LOG.trace("{}.onCreateContextMenu({}, {}, {})", getName(), menu, view, menuInfo);
		super.onCreateContextMenu(menu, view, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		LOG.trace("{}.onContextItemSelected({})", getName(), item);
		return super.onContextItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		LOG.trace("{}.onConfigurationChanged({})", getName(), newConfig);
		super.onConfigurationChanged(newConfig);
	}

	// Activity.onPause

	@Override
	public void onPause() {
		LOG.trace("{}.onPause", getName());
		super.onPause();
	}

	// Activity.onStop

	@Override
	public void onStop() {
		LOG.trace("{}.onStop", getName());
		super.onStop();
	}

	// Activity.onDestroy

	@Override
	public void onDestroyView() {
		LOG.trace("{}.onDestroyView", getName());
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		LOG.trace("{}.onDestroy", getName());
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		LOG.trace("{}.onDetach", getName());
		super.onDetach();
	}

	private String getName() {
		return getClass().getSimpleName() + "(" + LoggingHelper.getWho(this) + ")" + "@" + LoggingHelper.hash(this);
	}
}
