package net.twisterrob.inventory.android;

import androidx.annotation.NonNull;

import net.twisterrob.android.content.pref.ResourcePreferences;

public interface BaseComponent {
	@NonNull ResourcePreferences prefs();

	interface Provider {
		BaseComponent getBaseComponent();
	}
}
