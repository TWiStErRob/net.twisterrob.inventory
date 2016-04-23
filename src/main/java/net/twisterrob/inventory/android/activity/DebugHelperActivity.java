package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.content.Intent;
import android.os.*;
import android.view.WindowManager;

import com.android.debug.hv.ViewServer;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.BuildConfig;

/**
 * This class is debug-only but in a way that's available in release builds too.
 * This is to be able to just enable debugging in a release build and get some basic help for debug. 
 */
public class DebugHelperActivity extends VariantActivity {
	@SuppressWarnings("LoggerInitializedWithForeignClass") // looks better with BaseActivity
	private static final Logger LOG = LoggerFactory.getLogger(BaseActivity.class);

	@Override protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			LOG.debug("Creating {} {}\nextras={}\nsavedInstanceState={}",
					AndroidTools.toNameString(this),
					getIntent().toString(),
					AndroidTools.toLongString(getIntent().getExtras()),
					AndroidTools.toLongString(savedInstanceState)
			);
		}
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) {
			updateScreenKeeping();
			ViewServer.get(this).addWindow(this);
		}
	}

	@Override protected void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG) {
			updateScreenKeeping();
			ViewServer.get(this).setFocusedWindow(this);
		}
	}

	@Override protected void onDestroy() {
		if (BuildConfig.DEBUG) {
			ViewServer.get(this).removeWindow(this);
		}
		super.onDestroy();
	}

	/** @see <a href="http://stackoverflow.com/a/30037089/253468">How do I keep my screen unlocked during USB debugging?</a> */
	private void updateScreenKeeping() {
		if (BuildConfig.DEBUG && Debug.isDebuggerConnected()) {
			//LOG.trace("Keeping screen on for debugging, detach debugger and force an onResume to turn it off.");
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			//LOG.trace("Keeping screen on for debugging is now deactivated.");
		}
	}

	@Override protected void onNewIntent(Intent intent) {
		//setIntent(intent); // call this in child activity if you handle this event
		if (BuildConfig.DEBUG) {
			LOG.debug("Refreshing {} {}\n{}",
					AndroidTools.toNameString(this),
					AndroidTools.toString(intent.getExtras()),
					AndroidTools.toString(intent)
			);
		}
		super.onNewIntent(intent);
	}
}
