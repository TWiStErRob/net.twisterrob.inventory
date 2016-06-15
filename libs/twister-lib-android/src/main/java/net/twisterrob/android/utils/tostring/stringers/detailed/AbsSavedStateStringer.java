package net.twisterrob.android.utils.tostring.stringers.detailed;

import android.support.annotation.NonNull;
import android.view.AbsSavedState;

import net.twisterrob.android.utils.tostring.Stringer;

public class AbsSavedStateStringer implements Stringer<AbsSavedState> {
	@Override public @NonNull String toString(AbsSavedState object) {
		// FIXME type = null
		if (object == AbsSavedState.EMPTY_STATE) {
			return "AbsSavedState.EMPTY_STATE";
		} else {
			return "unknown SavedState: " + object;
			// AndroidTools.toString(object/* FIXME, object.getClass().getSuperclass()*/);
		}
	}
}
