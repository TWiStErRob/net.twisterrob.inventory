package net.twisterrob.inventory.android.content;

import android.content.Intent;

public abstract class VariantIntentService extends android.app.IntentService {
	public VariantIntentService(String name) {
		super(name);
	}

	@Override protected void onHandleIntent(Intent intent) {
		// NO OP, just for binary compatibility with LoggingIntentService
	}
}
