package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.android.debug.hv.ViewServer;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.App;

import static net.twisterrob.inventory.android.Constants.Dimensions.*;

public class BaseActivity extends ActionBarActivity {
	private static final Logger LOG = LoggerFactory.getLogger(BaseActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LOG.trace("Creating {}@{}\n{} {}",
				getClass().getSimpleName(), hashCode(), getIntent(), AndroidTools.toString(getIntent().getExtras()));
		super.onCreate(savedInstanceState);
		ViewServer.get(this).addWindow(this);
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
	protected <T extends Fragment> T getFragment(@IdRes int id) {
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
	public void setIcon(@RawRes int resourceId) {
		Drawable svg = App.pic().getSVG(this, resourceId, getActionbarIconSize(this), getActionbarIconPadding(this));
		setIcon(svg);
	}
}
