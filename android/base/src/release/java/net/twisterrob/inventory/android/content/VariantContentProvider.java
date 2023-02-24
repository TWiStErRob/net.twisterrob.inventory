package net.twisterrob.inventory.android.content;

import androidx.annotation.CallSuper;

public abstract class VariantContentProvider extends android.content.ContentProvider {

	@CallSuper
	@Override public boolean onCreate() {
		return true;
	}
}
