package android.support.v7.widget;

import java.util.*;

import android.support.annotation.NonNull;
import android.support.v7.widget.StaggeredGridLayoutManager.SavedState;

import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class StaggeredGridLayoutManagerSavedStateStringer implements Stringer<SavedState> {
	@Override public @NonNull String toString(SavedState state) {
		// TODO List<LazySpanLookup.FullSpanItem> mFullSpanItems = state.mFullSpanItems;
		return String.format(Locale.ROOT,
				"Anchor: {pos=%d, visPos=%d}, reverse=%b, RTL=%b, Spans: {offsets=%s, lookups=%s}",
				state.mAnchorPosition, state.mVisibleAnchorPosition, state.mReverseLayout, state.mLastLayoutRTL,
				toString(state.mSpanOffsetsSize, state.mSpanOffsets),
				toString(state.mSpanLookupSize, state.mSpanLookup));
	}

	private static String toString(int size, int... values) {
		if (size == 0 || values == null) {
			return "[]";
		} else if (size < 0) {
			return String.valueOf(size);
		}
		return Arrays.toString(values);
	}
}
