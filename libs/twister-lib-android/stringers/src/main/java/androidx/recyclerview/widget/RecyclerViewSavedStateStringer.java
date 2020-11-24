package androidx.recyclerview.widget;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.SavedState;

import net.twisterrob.java.utils.tostring.*;

public class RecyclerViewSavedStateStringer extends Stringer<SavedState> {
	@Override public void toString(@NonNull ToStringAppender append, SavedState state) {
		append.item(state.mLayoutState);
	}
}
