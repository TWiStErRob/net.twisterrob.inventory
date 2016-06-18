package net.twisterrob.android.utils.tostring.stringers.detailed;

import javax.annotation.Nonnull;

import android.support.design.widget.NavigationView.SavedState;

import net.twisterrob.java.utils.tostring.*;

public class NavigationViewSavedStateStringer extends Stringer<SavedState> {
	@Override public void toString(@Nonnull ToStringAppender append, SavedState state) {
		append.item(state.menuState);
	}
}
