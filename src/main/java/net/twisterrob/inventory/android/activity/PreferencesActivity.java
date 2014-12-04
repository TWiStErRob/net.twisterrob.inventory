package net.twisterrob.inventory.android.activity;

import android.content.*;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.*;

import net.twisterrob.inventory.android.*;

@SuppressWarnings("deprecation")
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onStart() {
		super.onStart();
		onSharedPreferenceChanged(getPrefs(), getString(R.string.pref_defaultEntityDetailsPage));
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPrefs().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		getPrefs().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);
		if (key.equals(getString(R.string.pref_defaultEntityDetailsPage))) {
			pref.setSummary(((ListPreference)pref).getEntry());
		}
	}

	private SharedPreferences getPrefs() {
		return getPreferenceScreen().getSharedPreferences();
	}

	public static Intent show() {
		Intent intent = new Intent(App.getAppContext(), PreferencesActivity.class);
		return intent;
	}
}
