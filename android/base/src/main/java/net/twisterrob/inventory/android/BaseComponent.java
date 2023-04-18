package net.twisterrob.inventory.android;

import androidx.annotation.NonNull;

import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.inventory.android.components.BuildInfo;
import net.twisterrob.inventory.android.components.Toaster;

public interface BaseComponent {
	@NonNull ResourcePreferences prefs();
	/**
	 * Untyped, so that base doesn't have to depend on database module.
	 * @return net.twisterrob.inventory.android.content.Database
	 */
	@NonNull Object db();
	@NonNull Toaster toaster();
	@NonNull BuildInfo buildInfo();

	interface Provider {
		BaseComponent getBaseComponent();
	}
}
