package net.twisterrob.inventory.android.activity;

import java.util.List;

import android.content.pm.*;
import android.os.Bundle;

import androidx.annotation.*;
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
	@Override public void onStart() {
		super.onStart();
		traversePreferences(getPreferenceScreen(), new PreferenceAction() {
			@Override public void before(@NonNull Preference preference) {
				if (preference.getClass() == Preference.class) {
					// .getClass() for exact match, don't care about implementing subclasses.
					if (preference.getIntent() != null) {
						PackageManager pm = requireActivity().getPackageManager();
						List<ResolveInfo> intents =
								pm.queryIntentActivities(preference.getIntent(), 0);
						preference.setEnabled(!intents.isEmpty());
					}
				}
			}
		});
	}

	interface PreferenceAction {
		void before(@NonNull Preference preference);
	}

	private static void traversePreferences(@NonNull PreferenceGroup parent,
			PreferenceAction action) {
		for (int i = 0; i < parent.getPreferenceCount(); ++i) {
			Preference pref = parent.getPreference(i);
			action.before(pref);
			if (pref instanceof PreferenceGroup) {
				traversePreferences((PreferenceGroup)pref, action);
			}
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
			fragment.setTargetFragment(this, 0);
			fragment.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
		} else {
			// The dialog is already showing.
		}
	}
}
