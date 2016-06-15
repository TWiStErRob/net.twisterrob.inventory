package android.support.v7.widget;

import java.util.Locale;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar.SavedState;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class SupportToolbarSavedStateStringer implements Stringer<Toolbar.SavedState> {
	private Context context;
	public SupportToolbarSavedStateStringer(Context context) {
		this.context = context;
	}

	@Override public @NonNull String toString(SavedState state) {
		return String.format(Locale.ROOT,
				"Overflow open=%b, Expanded MenuItem=%s",
				state.isOverflowOpen, AndroidTools.toNameString(context, state.expandedMenuItemId));
	}
}
