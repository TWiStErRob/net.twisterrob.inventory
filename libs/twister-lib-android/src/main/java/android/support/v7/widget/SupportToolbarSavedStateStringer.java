package android.support.v7.widget;

import javax.annotation.Nonnull;

import android.support.v7.widget.Toolbar.SavedState;

import net.twisterrob.android.utils.tostring.stringers.name.ResourceNameStringer;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.tostring.*;

@DebugHelper
public class SupportToolbarSavedStateStringer extends Stringer<SavedState> {
	@Override public void toString(@Nonnull ToStringAppender append, SavedState state) {
		append.booleanProperty(state.isOverflowOpen, "Overflow open", "Overflow closed");
		append.complexProperty("Expanded MenuItem", state.expandedMenuItemId, ResourceNameStringer.INSTANCE);
	}
}
