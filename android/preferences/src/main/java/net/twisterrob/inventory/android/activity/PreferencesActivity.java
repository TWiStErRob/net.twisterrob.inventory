package net.twisterrob.inventory.android.activity;

import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.*;

import net.twisterrob.inventory.android.preferences.R;

@SuppressWarnings("deprecation")
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}

	@Override protected void onStart() {
		super.onStart();
		SharedPreferences prefs = getPrefs();
		for (String prefKey : prefs.getAll().keySet()) {
			Preference pref = findPreference(prefKey);
			if (pref instanceof ListPreference) {
				onSharedPreferenceChanged(getPrefs(), prefKey);
			}
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
