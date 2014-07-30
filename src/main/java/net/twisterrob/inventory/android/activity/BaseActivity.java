package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.view.*;

import com.android.debug.hv.ViewServer;

import net.twisterrob.android.utils.tools.AndroidTools;

public class BaseActivity extends ActionBarActivity {
	private static final Logger LOG = LoggerFactory.getLogger(BaseActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LOG.trace("Creating {}: {}", this, AndroidTools.toString(getIntent().getExtras()));
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
		super.onDestroy();
		ViewServer.get(this).removeWindow(this);
	}

	private void initActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: // Respond to the action bar's Up/Home button
				Intent upIntent = NavUtils.getParentActivityIntent(this);
				if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
					TaskStackBuilder.create(this) // Create a new task with a synthesized back stack
							.addNextIntentWithParentStack(upIntent) // add the parents to the back stack
							.startActivities(); // Navigate up to the closest parent
				} else {
					NavUtils.navigateUpFromSameTask(this);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	protected <T> T getFragment(int id) {
		return (T)getSupportFragmentManager().findFragmentById(id);
	}
}
