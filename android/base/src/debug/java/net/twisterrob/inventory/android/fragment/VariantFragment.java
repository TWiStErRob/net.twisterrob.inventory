package net.twisterrob.inventory.android.fragment;

import androidx.loader.app.LoaderManager;

public class VariantFragment extends net.twisterrob.android.utils.log.LoggingFragment {
	@SuppressWarnings("deprecation")
	@Override public LoaderManager getLoaderManager() {
		return LoaderManager.getInstance(this);
	}
}
