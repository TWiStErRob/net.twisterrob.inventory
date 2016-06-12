package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.View;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.AndroidTools;

/**
 * Usage: <code>drawerLayout.addDrawerListener(new LoggingDrawerListener());</code>
 */
public class LoggingDrawerListener implements DrawerListener {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingDrawerListener.class);

	@Override public void onDrawerSlide(View drawerView, float slideOffset) {
		log("onDrawerSlide", drawerView, slideOffset);
	}
	@Override public void onDrawerOpened(View drawerView) {
		log("onDrawerOpened", drawerView);
	}
	@Override public void onDrawerClosed(View drawerView) {
		log("onDrawerClosed", drawerView);
	}
	@Override public void onDrawerStateChanged(int newState) {
		log("onDrawerStateChanged", AndroidTools.toDrawerLayoutStateString(newState));
	}

	private void log(String name, Object... args) {
		LoggingHelper.log(LOG, AndroidTools.toNameString(this), name, null, args);
	}
}
