package net.twisterrob.inventory.android.activity;

import java.util.List;

import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ResolveInfo;
import android.os.*;
import android.os.StrictMode.ThreadPolicy;
import android.preference.*;
import android.support.annotation.NonNull;

import net.twisterrob.inventory.android.preferences.R;

@SuppressWarnings("deprecation")
public class PreferencesActivity extends PreferenceActivity
		implements OnSharedPreferenceChangeListener {
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ThreadPolicy threadPolicy = StrictMode.allowThreadDiskWrites();
		try {
			// D/StrictMode: StrictMode policy violation; ~duration=73 ms: android.os.strictmode.DiskWriteViolation
			// at android.system.Os.mkdir(Os.java:375)
			// at android.app.ContextImpl.ensurePrivateDirExists(ContextImpl.java:648)
			// at android.app.ContextImpl.ensurePrivateDirExists(ContextImpl.java:636)
			// at android.app.ContextImpl.getPreferencesDir(ContextImpl.java:592)

			// D/StrictMode: StrictMode policy violation; ~duration=9 ms: android.os.strictmode.DiskReadViolation
			// at java.io.File.exists(File.java:815)
			// at android.app.ContextImpl.getDataDir(ContextImpl.java:2340)
			// at android.app.ContextImpl.getPreferencesDir(ContextImpl.java:590)

			// at android.app.ContextImpl.getPreferencesDir(continued from above 2)
			// at android.app.ContextImpl.getSharedPreferencesPath(ContextImpl.java:787)
			// at android.app.ContextImpl.getSharedPreferences(ContextImpl.java:439)
			// at android.content.ContextWrapper.getSharedPreferences(ContextWrapper.java:178)
			// at android.preference.PreferenceManager.getSharedPreferences(PreferenceManager.java:529)
			// at android.preference.Preference.getSharedPreferences(Preference.java:1233)
			// at android.preference.Preference.dispatchSetInitialValue(Preference.java:1586)
			// at android.preference.Preference.onAttachedToHierarchy(Preference.java:1358)
			// at android.preference.PreferenceInflater.onMergeRoots(PreferenceInflater.java:117)
			// at android.preference.PreferenceInflater.onMergeRoots(PreferenceInflater.java:44)
			// at android.preference.GenericInflater.inflate(GenericInflater.java:328)
			// at android.preference.GenericInflater.inflate(GenericInflater.java:271)
			// at android.preference.PreferenceManager.inflateFromResource(PreferenceManager.java:343)
			// at android.preference.PreferenceActivity.addPreferencesFromResource(PreferenceActivity.java:1541)
			addPreferencesFromResource(R.xml.preferences);
		} finally {
			StrictMode.setThreadPolicy(threadPolicy);
		}
	}

	@Override protected void onStart() {
		super.onStart();
		traversePreferences(getPreferenceScreen(), new PreferenceAction() {
			@Override public void before(@NonNull Preference preference) {
				if (preference.getClass() == Preference.class) {
					// .getClass() for exact match, no implementing subclasses
					if (preference.getIntent() != null) {
						List<ResolveInfo> intents = getPackageManager()
								.queryIntentActivities(preference.getIntent(), 0);
						preference.setEnabled(!intents.isEmpty());
					}
				}
			}

			@Override public void after(@NonNull Preference preference) {
				if (preference instanceof ListPreference && preference.getKey() != null) {
					onSharedPreferenceChanged(getPrefs(), preference.getKey());
				}
			}
		});
	}

	interface PreferenceAction {
		void before(@NonNull Preference preference);
		void after(@NonNull Preference preference);
	}

	private void traversePreferences(@NonNull PreferenceGroup parent, PreferenceAction action) {
		for (int i = 0; i < parent.getPreferenceCount(); ++i) {
			Preference pref = parent.getPreference(i);
			action.before(pref);
			if (pref instanceof PreferenceGroup) {
				traversePreferences((PreferenceGroup)pref, action);
			}
			action.after(pref);
		}
	}

	@Override protected void onResume() {
		super.onResume();
		getPrefs().registerOnSharedPreferenceChangeListener(this);
	}

	@Override protected void onPause() {
		getPrefs().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);
		if (pref instanceof ListPreference) {
			pref.setSummary(((ListPreference)pref).getEntry());
		}
	}

	private SharedPreferences getPrefs() {
		return getPreferenceScreen().getSharedPreferences();
	}

	public static Intent show(Context context) {
		return new Intent(context, PreferencesActivity.class);
	}
}
