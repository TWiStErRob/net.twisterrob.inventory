package net.twisterrob.android.utils.tostring.stringers.detailed;

import javax.annotation.Nonnull;

import android.os.Build.*;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView.SavedState;

import net.twisterrob.java.utils.tostring.*;

public class NavigationViewSavedStateStringer extends Stringer<SavedState> {
	@Override public void toString(@Nonnull ToStringAppender append, SavedState state) {
		Bundle menuState = state.menuState;
		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB && menuState != null) {
			// TODEL Only work around for 2.3.x; based on https://code.google.com/p/android/issues/detail?id=196430#c10
			menuState.setClassLoader(SavedState.class.getClassLoader());
		}
		append.item(menuState);
	}
}
