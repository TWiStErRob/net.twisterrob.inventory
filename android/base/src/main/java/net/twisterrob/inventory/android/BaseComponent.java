package net.twisterrob.inventory.android;

import android.content.Context;

import androidx.annotation.NonNull;

import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.inventory.android.components.BuildInfo;
import net.twisterrob.inventory.android.components.Toaster;
import net.twisterrob.inventory.android.content.Database;

public abstract class BaseComponent {

	public static @NonNull BaseComponent get(@NonNull Context context) {
		return ((Provider)context.getApplicationContext()).getBaseComponent();
	}

	public abstract @NonNull ResourcePreferences prefs();
	/**
	 * Untyped, so that base doesn't have to depend on database module.
	 *
	 * @return net.twisterrob.inventory.android.content.Database
	 */
	public abstract @NonNull Database db();
	public abstract @NonNull Toaster toaster();
	public abstract @NonNull BuildInfo buildInfo();

	interface Provider {
		BaseComponent getBaseComponent();
	}
}
