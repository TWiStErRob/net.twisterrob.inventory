package net.twisterrob.inventory.android.activity;

import android.content.Intent;

import androidx.annotation.ContentView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.loader.app.LoaderManager;

@UiThread // TODEL android.app.Activity should have this https://issuetracker.google.com/issues/80002899
public abstract class VariantActivity extends androidx.appcompat.app.AppCompatActivity {

	public VariantActivity() {
		super();
	}

	@ContentView
	public VariantActivity(@LayoutRes int contentLayoutId) {
		super(contentLayoutId);
	}

	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/255
	public @NonNull LoaderManager getSupportLoaderManager() {
		return LoaderManager.getInstance(this);
	}

	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/259
	@Override public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}
}
