package android.support.v4.widget;

import java.util.Locale;

import android.support.annotation.NonNull;

import net.twisterrob.android.annotation.*;
import net.twisterrob.android.utils.tools.AndroidTools;

public class SupportV4WidgetAccess {
	private static final DrawerLayoutStateStringer DRAWER_STRINGER = new DrawerLayoutStateStringer();

	public static String toString(Object value) {
		if (value instanceof DrawerLayout.SavedState) {
			return DRAWER_STRINGER.toString((DrawerLayout.SavedState)value);
		}
		return null;
	}
	public static boolean instanceOf(Object value) {
		return value instanceof DrawerLayout.SavedState;
	}

	private static class DrawerLayoutStateStringer {
		public @NonNull String toString(DrawerLayout.SavedState state) {
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
}
