package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.view.*;

import net.twisterrob.android.utils.tools.AndroidTools;

public class BaseActivity extends ActionBarActivity {
	private static final Logger LOG = LoggerFactory.getLogger(BaseActivity.class);

	private static final String EXTRA_REFERRER = "referrerPreviousOnStack";

	@Override
	protected void onCreate(Bundle arg0) {
		LOG.trace("Creating {}: {}", this, AndroidTools.toString(getIntent().getExtras()));
		super.onCreate(arg0);

		initActionBar();
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
				if (upIntent.getComponent().getClassName().equals(getLastActivity())) {
					//upIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
				} else {
					if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
						TaskStackBuilder.create(this) // Create a new task with a synthesized back stack
								.addNextIntentWithParentStack(upIntent) // add the parents to the back stack
								.startActivities(); // Navigate up to the closest parent
					} else {
						NavUtils.navigateUpFromSameTask(this);
					}
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	protected Intent createIntent(Class<? extends Activity> clazz) {
		Intent intent = new Intent(getApplicationContext(), clazz);
		intent.putExtra(EXTRA_REFERRER, RoomsActivity.class.getName());
		return intent;
	}

	public String getLastActivity() {
		return getIntent().getStringExtra(EXTRA_REFERRER);
	}
}
