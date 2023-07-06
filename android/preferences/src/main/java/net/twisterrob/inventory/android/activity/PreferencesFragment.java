package net.twisterrob.inventory.android.activity;

import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.*;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.settings.view.*;
import net.twisterrob.inventory.android.preferences.R;

@AndroidEntryPoint
public class PreferencesFragment extends PreferenceFragmentCompat {

	private static final String DIALOG_FRAGMENT_TAG =
			"net.twisterrob.inventory.android.activity.PreferencesFragment.DIALOG";

	@Override public void onCreatePreferences(
			@Nullable Bundle savedInstanceState,
			@Nullable String rootKey
	) {
		/*
		 * API 30 / Android 11 / R emulator
		 * ```
		 * StrictMode policy violation; ~duration=42 ms: android.os.strictmode.DiskReadViolation
		 * 	at android.os.StrictMode$AndroidBlockGuardPolicy.onReadFromDisk(StrictMode.java:1596)
		 * 	at java.io.File.exists(File.java:815)
		 * 	at android.app.ContextImpl.getDataDir(ContextImpl.java:2539)
		 * 	at android.app.ContextImpl.getPreferencesDir(ContextImpl.java:626)
		 * 	at android.app.ContextImpl.getSharedPreferencesPath(ContextImpl.java:853)
		 * 	at android.app.ContextImpl.getSharedPreferences(ContextImpl.java:475)
		 * 	at androidx.preference.PreferenceFragmentCompat.setPreferencesFromResource(PreferenceFragmentCompat.java:377)
		 * 	at net.twisterrob.inventory.android.activity.PreferencesFragment.onCreatePreferences(PreferencesFragment.java:21)
		 * 	at androidx.preference.PreferenceFragmentCompat.onCreate(PreferenceFragmentCompat.java:160)
		 * 	at androidx.fragment.app.FragmentActivity.onCreateView(FragmentActivity.java:335)
		 * 	at net.twisterrob.android.utils.log.LoggingActivity.onCreateView(LoggingActivity.java:77)
		 * 	at android.view.LayoutInflater.inflate(LayoutInflater.java:479)
		 * 	at androidx.appcompat.app.AppCompatDelegateImpl.setContentView(AppCompatDelegateImpl.java:696)
		 * 	at androidx.appcompat.app.AppCompatActivity.setContentView(AppCompatActivity.java:170)
		 * 	at net.twisterrob.inventory.android.activity.PreferencesActivity.onCreate(PreferencesActivity.java:13)
		 * ```
		 */
		StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		try {
			setPreferencesFromResource(R.xml.preferences, rootKey);
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}

	@Override public void onDisplayPreferenceDialog(Preference preference) {
		if (!(preference instanceof NumberPickerPreference)) {
			super.onDisplayPreferenceDialog(preference);
			return;
		}
		if (getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
			DialogFragment fragment =
					NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
			setTarget(fragment);
			fragment.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
		} else {
			// The dialog is already showing.
		}
	}

	@SuppressWarnings("deprecation") // TODEL https://issuetracker.google.com/issues/181793702
	private void setTarget(@NonNull DialogFragment fragment) {
		fragment.setTargetFragment(this, 0);
	}
}
