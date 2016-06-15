package android.support.v4.app;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment.SavedState;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;

public class SupportFragmentSavedStateStringer implements Stringer<Fragment.SavedState> {
	@Override public @NonNull String toString(SavedState object) {
		// FIXME type = "v4.Fragment.SavedState";
		return AndroidTools.toString(object.mState);
	}
}
