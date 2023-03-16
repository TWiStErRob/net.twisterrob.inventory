package net.twisterrob.inventory.android.content;

import android.content.Intent;
import androidx.annotation.NonNull;

@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/256
public abstract class VariantIntentService extends androidx.core.app.JobIntentService {
	@Override protected void onHandleWork(@NonNull Intent intent) {
		// NO OP, just for binary compatibility with LoggingJobIntentService
	}
}
