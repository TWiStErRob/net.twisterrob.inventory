package net.twisterrob.inventory.android.content;

import android.content.Context;

import androidx.annotation.NonNull;

public abstract class BroadcastTools {

	private BroadcastTools() {
		// Prevent instatiation.
	}

	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/256
	public static @NonNull androidx.localbroadcastmanager.content.LocalBroadcastManager
	getLocalBroadcastManager(@NonNull Context context) {
		return androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context);
	}
}
