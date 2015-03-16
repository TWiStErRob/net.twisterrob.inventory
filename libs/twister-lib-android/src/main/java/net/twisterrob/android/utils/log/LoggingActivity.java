package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBarActivity;
import android.view.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.java.utils.*;

public class LoggingActivity extends ActionBarActivity {
	private static final Logger LOG = LoggerFactory.getLogger("Activity");

	public LoggingActivity() {
		LOG.trace("{}.ctor", getName());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LOG.trace("{}.onCreate({})", getName(), AndroidTools.toShortString(savedInstanceState));
		super.onCreate(savedInstanceState);
		LoaderManager lm = getSupportLoaderManager();
		LOG.trace("{}.loaderManager={}({})", getName(), lm, ReflectionTools.get(lm, "mWho"));
	}

	@Override
	protected void onStart() {
		LOG.trace("{}.onStart()", getName());
		super.onStart();
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		LOG.trace("{}.onRestoreInstanceState({})", getName(), AndroidTools.toShortString(savedInstanceState));
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		LOG.trace("{}.onPostCreate({})", getName(), AndroidTools.toShortString(savedInstanceState));
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		LOG.trace("{}.onResume()", getName());
		super.onResume();
	}

	@Override
	protected void onPostResume() {
		LOG.trace("{}.onPostResume()", getName());
		super.onPostResume();
	}

	@Override
	protected void onResumeFragments() {
		LOG.trace("{}.onResumeFragments()", getName());
		super.onResumeFragments();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		LOG.trace("{}.onCreateOptionsMenu({})", getName(), menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		LOG.trace("{}.onPrepareOptionsMenu({})", getName(), menu);
		return super.onPrepareOptionsMenu(menu);
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
	protected void onPause() {
		LOG.trace("{}.onPause()", getName());
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		LOG.trace("{}.onSaveInstanceState({})", getName(), AndroidTools.toShortString(outState));
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		LOG.trace("{}.onStop()", getName());
		super.onStop();
	}

	@Override
	protected void onRestart() {
		LOG.trace("{}.onRestart()", getName());
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		LOG.trace("{}.onDestroy()", getName());
		super.onDestroy();
	}

	private String getName() {
		return getClass().getSimpleName() + "@" + StringTools.hashString(this);
	}
}
