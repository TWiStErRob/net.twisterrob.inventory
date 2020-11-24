package net.twisterrob.android.utils.tostring.stringers.detailed;

import android.view.AbsSavedState;

import androidx.annotation.NonNull;

import net.twisterrob.java.utils.tostring.*;

public class AbsSavedStateStringer extends Stringer<AbsSavedState> {
	@Override public String getType(AbsSavedState object) {
		return null;
	}
	@Override public void toString(@NonNull ToStringAppender append, AbsSavedState object) {
		if (object == AbsSavedState.EMPTY_STATE) {
			append.selfDescribingProperty("AbsSavedState.EMPTY_STATE");
		} else {
			append.selfDescribingProperty("unknown SavedState: " + object);
			// FIXME somehow start from object.getClass().getSuperclass()
		}
	}
}
