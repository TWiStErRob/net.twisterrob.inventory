package android.support.v4.app;

import javax.annotation.Nonnull;

import android.support.v4.app.Fragment.SavedState;

import net.twisterrob.java.utils.tostring.*;

public class SupportFragmentSavedStateStringer extends Stringer<SavedState> {
	@Override public String getType(SavedState object) {
		return "v4.Fragment.SavedState";
	}
	@Override public void toString(@Nonnull ToStringAppender append, SavedState object) {
		append.item(object.mState);
	}
}
