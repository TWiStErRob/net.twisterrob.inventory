package net.twisterrob.inventory.android.content;

import android.content.Intent;
import androidx.annotation.NonNull;

public abstract class VariantIntentService extends androidx.core.app.JobIntentService {
	@Override protected void onHandleWork(@NonNull Intent intent) {
		// NO OP, just for binary compatibility with LoggingJobIntentService
	}
}
