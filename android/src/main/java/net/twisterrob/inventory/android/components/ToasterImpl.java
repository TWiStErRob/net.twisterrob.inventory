package net.twisterrob.inventory.android.components;

import androidx.annotation.NonNull;

import net.twisterrob.inventory.android.App;

class ToasterImpl implements Toaster {
	@Override public void toast(@NonNull CharSequence message) {
		App.toast(message);
	}
}
