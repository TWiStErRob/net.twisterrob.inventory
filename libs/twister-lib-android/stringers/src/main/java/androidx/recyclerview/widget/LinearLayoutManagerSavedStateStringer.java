package androidx.recyclerview.widget;

import javax.annotation.Nonnull;

import androidx.recyclerview.widget.LinearLayoutManager.SavedState;

import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.tostring.*;

@DebugHelper
public class LinearLayoutManagerSavedStateStringer extends Stringer<SavedState> {
	@Override public void toString(@Nonnull ToStringAppender append, SavedState state) {
		append.beginPropertyGroup("Anchor");
		append.rawProperty("pos", state.mAnchorPosition);
		append.rawProperty("offset", state.mAnchorOffset);
		append.rawProperty("fromEnd", state.mAnchorLayoutFromEnd);
		append.endPropertyGroup();
	}
}
