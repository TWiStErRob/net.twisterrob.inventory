package net.twisterrob.inventory.android;

import android.content.Context;

import androidx.annotation.NonNull;

public abstract class BaseComponent {

	/**
	 * @deprecated inject {@link net.twisterrob.inventory.android.content.Database} directly.
	 */
	@SuppressWarnings({"deprecation", "JavadocReference"})
	@Deprecated
	public static @NonNull BaseComponent get(@NonNull Context context) {
		return ((Provider)context.getApplicationContext()).getBaseComponent();
	}

	/**
	 * Untyped, so that base doesn't have to depend on database module.
	 *
	 * @return net.twisterrob.inventory.android.content.Database
	 * @deprecated inject {@link net.twisterrob.inventory.android.content.Database} directly.
	 */
	@SuppressWarnings("JavadocReference")
	@Deprecated
	public abstract @NonNull Object db();

	interface Provider {
		/**
		 * @deprecated inject {@link net.twisterrob.inventory.android.content.Database} directly.
		 */
		@Deprecated
		@SuppressWarnings("JavadocReference")
		BaseComponent getBaseComponent();
	}
}
