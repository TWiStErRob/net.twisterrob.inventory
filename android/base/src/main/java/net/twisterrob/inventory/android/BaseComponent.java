package net.twisterrob.inventory.android;

import androidx.annotation.NonNull;

import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.inventory.android.components.Toaster;

public interface BaseComponent {
	@NonNull ResourcePreferences prefs();
	@NonNull Toaster toaster();

	interface Provider {
		BaseComponent getBaseComponent();
	}
}
