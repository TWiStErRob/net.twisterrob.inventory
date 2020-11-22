package net.twisterrob.inventory.android.fragment;

import androidx.loader.app.LoaderManager;

public class VariantFragment extends androidx.fragment.app.Fragment {
	@SuppressWarnings("deprecation")
	@Override public LoaderManager getLoaderManager() {
		return LoaderManager.getInstance(this);
	}
}
