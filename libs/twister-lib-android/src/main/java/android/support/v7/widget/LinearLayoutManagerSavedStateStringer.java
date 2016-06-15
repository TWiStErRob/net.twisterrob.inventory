package android.support.v7.widget;

import java.util.Locale;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager.SavedState;

import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class LinearLayoutManagerSavedStateStringer implements Stringer<SavedState> {
	@Override public @NonNull String toString(SavedState state) {
		return String.format(Locale.ROOT,
				"Anchor: {pos=%d, offset=%d, fromEnd=%b}",
				state.mAnchorPosition, state.mAnchorOffset, state.mAnchorLayoutFromEnd);
	}
}
