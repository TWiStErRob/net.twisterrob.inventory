package net.twisterrob.inventory.android.content.db;

import java.util.Locale;

import org.slf4j.*;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.java.utils.StringTools;

import static net.twisterrob.android.app.BaseApp.*;

public class LanguageUpdater {
	private static final Logger LOG = LoggerFactory.getLogger(LanguageUpdater.class);
	private final ResourcePreferences prefs;
	private final Database db;

	public LanguageUpdater(ResourcePreferences prefs, Database db) {
		this.prefs = prefs;
		this.db = db;
	}

	public void updateLanguage(@NonNull Locale newLocale) {
		String storedLanguage = prefs.getString(R.string.pref_currentLanguage, null);
		String currentLanguage = newLocale.toString();
		if (!currentLanguage.equals(storedLanguage)) {
			String from = StringTools.toLocale(storedLanguage).getDisplayName();
			String to = StringTools.toLocale(currentLanguage).getDisplayName();
			String message = getAppContext().getString(R.string.message_locale_changed, from, to);
			LOG.debug(message);
			if (!TextUtils.isEmpty(from)) {
				App.toast(message);
			}
			try {
				db.updateCategoryCache(getAppContext());
				LOG.debug("Locale update successful: {} -> {}", storedLanguage, currentLanguage);
				prefs.setString(R.string.pref_currentLanguage, currentLanguage);
			} catch (Exception ex) {
				LOG.error("Locale update failed: {} -> {}", storedLanguage, currentLanguage, ex);
			}
		}
	}
}
