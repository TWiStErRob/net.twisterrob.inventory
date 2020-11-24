package androidx.fragment.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment.SavedState;

import net.twisterrob.java.utils.tostring.*;

public class SupportFragmentSavedStateStringer extends Stringer<SavedState> {
	@Override public String getType(SavedState object) {
		return "v4.Fragment.SavedState";
	}
	@Override public void toString(@NonNull ToStringAppender append, SavedState object) {
		append.item(object.mState);
	}
}
