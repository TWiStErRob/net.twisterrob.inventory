package net.twisterrob.android.utils.tostring.stringers.detailed;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.SavedState;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;

public class NavigationViewSavedStateStringer implements Stringer<NavigationView.SavedState> {
	@Override public @NonNull String toString(SavedState state) {
		return AndroidTools.toString(state.menuState);
	}
}
