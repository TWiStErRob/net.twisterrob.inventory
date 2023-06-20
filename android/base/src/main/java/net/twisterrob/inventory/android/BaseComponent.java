package net.twisterrob.inventory.android;

import android.content.Context;

import androidx.annotation.NonNull;

public abstract class BaseComponent {

	public static @NonNull BaseComponent get(@NonNull Context context) {
		return ((Provider)context.getApplicationContext()).getBaseComponent();
	}

	/**
	 * Untyped, so that base doesn't have to depend on database module.
	 * Use {@code Database.get(Context)} instead of this.
	 *
	 * @return net.twisterrob.inventory.android.content.Database
	 */
	public abstract @NonNull Object db();

	interface Provider {
		BaseComponent getBaseComponent();
	}
}
