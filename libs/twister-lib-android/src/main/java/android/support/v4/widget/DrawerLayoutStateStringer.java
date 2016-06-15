package android.support.v4.widget;

import java.util.Locale;

import android.support.annotation.NonNull;

import net.twisterrob.android.annotation.*;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;

public class DrawerLayoutStateStringer implements Stringer<DrawerLayout.SavedState> {
	@Override public @NonNull String toString(DrawerLayout.SavedState state) {
		if (state == null) {
			return AndroidTools.NULL;
		}
		return String.format(Locale.ROOT, "OpenDrawer=%s, LockMode: {left=%s, right=%s, start=%s, end=%s}",
				GravityFlag.Converter.toString(state.openDrawerGravity),
				LockMode.Converter.toString(state.lockModeLeft),
				LockMode.Converter.toString(state.lockModeRight),
				LockMode.Converter.toString(state.lockModeStart),
				LockMode.Converter.toString(state.lockModeEnd)
		);
	}
}
