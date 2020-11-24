package net.twisterrob.inventory.android.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.*;

import net.twisterrob.android.settings.view.*;
import net.twisterrob.inventory.android.preferences.R;

public class PreferencesFragment extends PreferenceFragmentCompat {

	private static final String DIALOG_FRAGMENT_TAG =
			"net.twisterrob.inventory.android.activity.PreferencesFragment.DIALOG";

	@Override public void onCreatePreferences(
			@Nullable Bundle savedInstanceState,
			@Nullable String rootKey
	) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}

	@Override public void onDisplayPreferenceDialog(Preference preference) {
		if (!(preference instanceof NumberPickerPreference)) {
			super.onDisplayPreferenceDialog(preference);
			return;
		}
		if (getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
			DialogFragment fragment =
					NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.getKey());
			fragment.setTargetFragment(this, 0);
			fragment.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
		} else {
			// The dialog is already showing.
		}
	}
}
