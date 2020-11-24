package net.twisterrob.android.utils.tostring.stringers.detailed;

import com.google.android.material.navigation.NavigationView.SavedState;

import androidx.annotation.NonNull;

import net.twisterrob.java.utils.tostring.*;

public class NavigationViewSavedStateStringer extends Stringer<SavedState> {
	@Override public void toString(@NonNull ToStringAppender append, SavedState state) {
		append.item(state.menuState);
	}
}
