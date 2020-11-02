package net.twisterrob.inventory.android.activity;

import android.support.annotation.UiThread;
import android.support.v4.app.LoaderManager;

@UiThread // TODEL android.app.Activity should have this https://issuetracker.google.com/issues/80002899
public abstract class VariantActivity extends android.support.v7.app.AppCompatActivity {
	@SuppressWarnings("deprecation")
	public LoaderManager getSupportLoaderManager() {
		return LoaderManager.getInstance(this);
	}
}
