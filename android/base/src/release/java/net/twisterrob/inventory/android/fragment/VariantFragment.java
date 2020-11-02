package net.twisterrob.inventory.android.fragment;

import android.support.v4.app.LoaderManager;

public class VariantFragment extends android.support.v4.app.Fragment {
	@SuppressWarnings("deprecation")
	@Override public LoaderManager getLoaderManager() {
		return LoaderManager.getInstance(this);
	}
}
