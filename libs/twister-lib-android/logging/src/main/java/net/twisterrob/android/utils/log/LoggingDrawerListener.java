package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener;

import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;

/**
 * Usage: <code>drawerLayout.addDrawerListener(new LoggingDrawerListener());</code>
 */
public class LoggingDrawerListener implements DrawerListener {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingDrawerListener.class);

	@Override public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
		log("onDrawerSlide", drawerView, slideOffset);
	}
	@Override public void onDrawerOpened(@NonNull View drawerView) {
		log("onDrawerOpened", drawerView);
	}
	@Override public void onDrawerClosed(@NonNull View drawerView) {
		log("onDrawerClosed", drawerView);
	}
	@Override public void onDrawerStateChanged(int newState) {
		log("onDrawerStateChanged", StringerTools.toDrawerLayoutStateString(newState));
	}

	private void log(String name, Object... args) {
		LoggingHelper.log(LOG, StringerTools.toNameString(this), name, null, args);
	}
}
