package androidx.recyclerview.widget;

import java.util.Arrays;

import javax.annotation.Nonnull;

import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.tostring.*;

@DebugHelper
public class StaggeredGridLayoutManagerSavedStateStringer extends Stringer<StaggeredGridLayoutManager.SavedState> {
	@Override public void toString(@Nonnull ToStringAppender append, StaggeredGridLayoutManager.SavedState state) {
		// TODO List<LazySpanLookup.FullSpanItem> mFullSpanItems = state.mFullSpanItems;
		append.beginPropertyGroup("Anchor");
		append.rawProperty("pos", state.mAnchorPosition);
		append.rawProperty("visPos", state.mVisibleAnchorPosition);
		append.endPropertyGroup();

		append.rawProperty("reverse", state.mReverseLayout);
		append.rawProperty("RTL", state.mLastLayoutRTL);

		append.beginPropertyGroup("Spans");
		append.rawProperty("offsets", toString(state.mSpanOffsetsSize, state.mSpanOffsets));
		append.rawProperty("lookups", toString(state.mSpanLookupSize, state.mSpanLookup));
		append.endPropertyGroup();
	}

	// FIXME try to get rid of this
	private static String toString(int size, int... values) {
		if (size == 0 || values == null) {
			return "[]";
		} else if (size < 0) {
			return String.valueOf(size);
		}
		return Arrays.toString(values);
	}
}
