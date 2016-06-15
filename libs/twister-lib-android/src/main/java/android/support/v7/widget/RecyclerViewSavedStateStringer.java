package android.support.v7.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.SavedState;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;

public class RecyclerViewSavedStateStringer implements Stringer<SavedState> {
	@NonNull @Override public String toString(SavedState state) {
		return AndroidTools.toString(state.mLayoutState);
	}
}
