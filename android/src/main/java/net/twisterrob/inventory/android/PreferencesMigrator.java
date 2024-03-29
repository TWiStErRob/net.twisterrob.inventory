package net.twisterrob.inventory.android;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources;

import net.twisterrob.android.content.pref.ResourcePreferences;

public class PreferencesMigrator {
	private static final Logger LOG = LoggerFactory.getLogger(PreferencesMigrator.class);
	private static final int VERSION_INITIAL_RELEASE = 0;

	private final Resources res;
	private final ResourcePreferences prefs;

	public PreferencesMigrator(Context context, ResourcePreferences prefs) {
		this.res = context.getResources();
		this.prefs = prefs;
	}

	@SuppressWarnings("fallthrough")
	public void migrate() {
		// If the version is missing, that means the preferences are
		// in the state of the first release, which had no prefs version.
		// All older versions will have the prefs version properly initialized at startup.
		int oldVersion = prefs.getIntVal(R.string.pref_version, VERSION_INITIAL_RELEASE);
		int newVersion = res.getInteger(R.integer.pref_version_default);
		if (newVersion == oldVersion) {
			LOG.info("Preferences are up to date at v{}", newVersion);
			return; // Don't bother, if version didn't change.
		} else if (newVersion < oldVersion) {
			// CONSIDER throwing
			LOG.info("Preferences version is invalid: old={}, new={}", oldVersion, newVersion);
			return; // Something is weird, a downgrade is hacky, just leave it.
		} else {
			LOG.info("Updating preferences from v{} to v{}", oldVersion, newVersion);
			// No `return`, see `switch` below.
		}
		// Brings oldVersion up to date with current code, EVERYTHING FALLS THROUGH!
		switch (oldVersion) {
			case VERSION_INITIAL_RELEASE: {
				// Most users probably didn't touch the default page setting, so migrate them to automatic display.
				// Those who did touch it, know where it is, and can always set it back to image.
				String oldPage = prefs.getString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_image);
				if (oldPage.equals(res.getString(R.string.pref_defaultViewPage_image))) {
					prefs.setString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_auto);
				}
			}
			case 1: {
				// R.string.pref_state_backup_path was removed, because Backup screen was fully rewritten.
				prefs.edit().remove("backupPath");
			}
			case 2: {
				// Increase R.integer.pref_version_default, and add cases above to handled migrations.
			}
			default:
				prefs.setIntVal(R.string.pref_version, newVersion);
		}
	}
}
