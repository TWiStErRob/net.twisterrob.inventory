package net.twisterrob.inventory.android.fragment;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.ContentView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;

public class VariantFragment extends net.twisterrob.android.utils.log.LoggingFragment {

	public VariantFragment() {
		super();
	}

	@ContentView
	public VariantFragment(@LayoutRes int contentLayoutId) {
		super(contentLayoutId);
	}

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

	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/262
	@Override public void setHasOptionsMenu(boolean hasMenu) {
		super.setHasOptionsMenu(hasMenu);
	}
	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/262
	@Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
		super.onCreateOptionsMenu(menu, menuInflater);
	}
	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/262
	@Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}
	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/262
	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/262
	@Override public void onOptionsMenuClosed(@NonNull Menu menu) {
		super.onOptionsMenuClosed(menu);
	}
	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/262
	@Override public void onDestroyOptionsMenu() {
		super.onDestroyOptionsMenu();
	}
}
