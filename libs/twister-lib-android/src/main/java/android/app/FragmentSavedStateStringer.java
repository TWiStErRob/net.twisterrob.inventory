package android.app;

import javax.annotation.Nonnull;

import android.annotation.TargetApi;
import android.app.Fragment.SavedState;
import android.os.Build.VERSION_CODES;

import net.twisterrob.java.utils.ReflectionTools;
import net.twisterrob.java.utils.tostring.*;

@TargetApi(VERSION_CODES.HONEYCOMB)
public class FragmentSavedStateStringer extends Stringer<SavedState> {
	@Override public String getType(SavedState object) {
		return "Fragment.SavedState";
	}
	@Override public void toString(@Nonnull ToStringAppender append, SavedState state) {
		// mState is package private, but probably @hide too, because it's not accessible
		append.item(ReflectionTools.get(state, "mState"));
	}
}
