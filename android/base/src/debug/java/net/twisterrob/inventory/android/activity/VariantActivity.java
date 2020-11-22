package net.twisterrob.inventory.android.activity;

import androidx.annotation.UiThread;
import androidx.loader.app.LoaderManager;

@UiThread // TODEL android.app.Activity should have this https://issuetracker.google.com/issues/80002899
public abstract class VariantActivity extends net.twisterrob.android.utils.log.LoggingActivity {
	@SuppressWarnings("deprecation")
	public LoaderManager getSupportLoaderManager() {
		return LoaderManager.getInstance(this);
	}
}
