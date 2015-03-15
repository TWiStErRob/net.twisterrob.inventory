package net.twisterrob.inventory.android.view;

import java.lang.reflect.Field;

import org.slf4j.*;

import android.support.v4.app.*;

public class LoggingHelper {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingHelper.class);

	public static String toString(LoaderManager manager) {
		return manager.toString() + "(" + getWho(manager) + ")";
	}

	public static String getWho(LoaderManager manager) {
		try {
			Field who = manager.getClass().getDeclaredField("mWho");
			who.setAccessible(true);
			return (String)who.get(manager);
		} catch (Exception ex) {
			LOG.warn("Cannot get LoaderManager.mWho", ex);
			return null;
		}
	}
	public static String getWho(Fragment fragment) {
		try {
			Field who = Fragment.class.getDeclaredField("mWho");
			who.setAccessible(true);
			return (String)who.get(fragment);
		} catch (Exception ex) {
			LOG.warn("Cannot read Fragment.mWho", ex);
			return null;
		}
	}
	public static String hash(Object object) {
		return Integer.toHexString(System.identityHashCode(object));
	}
}
