package net.twisterrob.inventory.android.activity;

import java.util.List;

import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.*;
import android.support.annotation.NonNull;

import net.twisterrob.inventory.android.preferences.R;

@SuppressWarnings("deprecation")
public class PreferencesActivity extends PreferenceActivity
		implements OnSharedPreferenceChangeListener {
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
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
