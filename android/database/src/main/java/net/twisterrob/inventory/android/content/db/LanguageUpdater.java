package net.twisterrob.inventory.android.content.db;

import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.*;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.inventory.android.components.Toaster;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.database.R;
import net.twisterrob.java.utils.StringTools;

public class LanguageUpdater {
	private static final Logger LOG = LoggerFactory.getLogger(LanguageUpdater.class);
	private final Context appContext;
	private final ResourcePreferences prefs;
	private final Database db;
	private final Toaster toaster;

	@Inject
	public LanguageUpdater(
			@ApplicationContext Context appContext,
			ResourcePreferences prefs,
			Database db,
			Toaster toaster
	) {
		this.appContext = appContext;
		this.prefs = prefs;
		this.db = db;
		this.toaster = toaster;
	}

	public void updateLanguage(@NonNull Locale newLocale) {
		String storedLanguage = prefs.getString(R.string.pref_currentLanguage, null);
		String currentLanguage = newLocale.toString();
		if (!currentLanguage.equals(storedLanguage)) {
			String from = StringTools.toLocale(storedLanguage).getDisplayName();
			String to = StringTools.toLocale(currentLanguage).getDisplayName();
			String message = appContext.getString(R.string.message_locale_changed, from, to);
			LOG.debug(message);
			if (!TextUtils.isEmpty(from)) {
				toaster.toast(message);
			}
			try {
				db.updateCategoryCache(appContext);
				LOG.debug("Locale update successful: {} -> {}", storedLanguage, currentLanguage);
				prefs.setString(R.string.pref_currentLanguage, currentLanguage);
			} catch (Exception ex) {
				LOG.error("Locale update failed: {} -> {}", storedLanguage, currentLanguage, ex);
			}
		}
	}
}
