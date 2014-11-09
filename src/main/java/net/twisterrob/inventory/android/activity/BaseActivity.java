package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.*;
import android.view.*;

import com.android.debug.hv.ViewServer;

import net.twisterrob.android.utils.tools.AndroidTools;

public class BaseActivity extends ActionBarActivity {
	private static final Logger LOG = LoggerFactory.getLogger(BaseActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LOG.trace("Creating {}@{}\n{} {}",
				getClass().getSimpleName(), hashCode(), getIntent(), AndroidTools.toString(getIntent().getExtras()));
		super.onCreate(savedInstanceState);
		ViewServer.get(this).addWindow(this);
		initActionBar();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ViewServer.get(this).setFocusedWindow(this);
	}

	@Override
	protected void onDestroy() {
		ViewServer.get(this).removeWindow(this);
		super.onDestroy();
	}

	@SuppressLint({"NewApi", "InlinedApi"})
	private void initActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		// TODO true, but needs onOptionsItemSelected and onCreateSupportNavigateUpTaskStack
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);

		initActionBarSoftwareRendering();
	}

	private void initActionBarSoftwareRendering() {
		View actionBarView = findViewById(getResources().getIdentifier("action_bar", "id", getPackageName()));
		if (actionBarView == null) {
			actionBarView = findViewById(getResources().getIdentifier("action_bar", "id", "android"));
		}
		if (actionBarView != null) {
			ViewCompat.setLayerType(actionBarView, ViewCompat.LAYER_TYPE_SOFTWARE, null);
		} else {
			LOG.warn("Unable to set SOFTWARE rendering layer on action bar");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: // Respond to the action bar's Up/Home button
				if (false) {
					Intent upIntent = NavUtils.getParentActivityIntent(this);
					if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
						TaskStackBuilder.create(this) // Create a new task with a synthesized back stack
								.addNextIntentWithParentStack(upIntent) // add the parents to the back stack
								.startActivities(); // Navigate up to the closest parent
					} else {
						NavUtils.navigateUpFromSameTask(this);
						//					Intent intent = NavUtils.getParentActivityIntent(this);
						//					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						//					NavUtils.navigateUpTo(this, intent);
					}
					return true;
				}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends Fragment> T getFragment(String tag) {
		return (T)getSupportFragmentManager().findFragmentByTag(tag);
	}
	@SuppressWarnings("unchecked")
	protected <T extends Fragment> T getFragment(int id) {
		return (T)getSupportFragmentManager().findFragmentById(id);
	}

	protected void showDialog(DialogFragment dialog) {
		dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
	}

	public void setActionBarSubtitle(CharSequence string) {
		getSupportActionBar().setSubtitle(string);
	}
	public void setActionBarTitle(CharSequence string) {
		getSupportActionBar().setTitle(string);
	}

	public void setIcon(Drawable iconDrawable) {
		getSupportActionBar().setIcon(iconDrawable);
	}
}
