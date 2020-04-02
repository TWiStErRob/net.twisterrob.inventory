package net.twisterrob.android.wiring;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView.OnQueryTextListener;

public class CollapseActionViewOnSubmit implements OnQueryTextListener {
	private final @NonNull ActionBar actionBar;

	public CollapseActionViewOnSubmit(@NonNull ActionBar actionBar) {
		this.actionBar = actionBar;
	}

	@SuppressLint("RestrictedApi")
	// TODO find a public way of achieving the same effect
	@Override public boolean onQueryTextSubmit(String query) {
		actionBar.collapseActionView();
		return false;
	}

	@Override public boolean onQueryTextChange(String query) {
		return false;
	}
}
