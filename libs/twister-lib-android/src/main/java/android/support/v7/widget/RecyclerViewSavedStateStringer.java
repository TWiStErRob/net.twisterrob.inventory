package android.support.v7.widget;

import javax.annotation.Nonnull;

import android.support.v7.widget.RecyclerView.SavedState;

import net.twisterrob.java.utils.tostring.*;

public class RecyclerViewSavedStateStringer extends Stringer<SavedState> {
	@Override public void toString(@Nonnull ToStringAppender append, SavedState state) {
		append.item(state.mLayoutState);
	}
}
