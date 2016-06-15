package android.app;

import android.annotation.TargetApi;
import android.app.Fragment.SavedState;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.java.utils.ReflectionTools;

@TargetApi(VERSION_CODES.HONEYCOMB)
public class FragmentSavedStateStringer implements Stringer<Fragment.SavedState> {
	@Override public @NonNull String toString(SavedState state) {
		// FIXME type = "Fragment.SavedState";
		return AndroidTools.toString(ReflectionTools.get(state, "mState"));
	}
}
