package net.twisterrob.inventory.android.fragment;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;

public class VariantFragment extends androidx.fragment.app.Fragment {
	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/255
	@Override public LoaderManager getLoaderManager() {
		return LoaderManager.getInstance(this);
	}

	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/259
	@Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/259
	@Override public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}
}
