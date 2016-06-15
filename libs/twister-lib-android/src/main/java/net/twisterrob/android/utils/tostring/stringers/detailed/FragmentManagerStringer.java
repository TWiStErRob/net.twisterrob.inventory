package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.util.Locale;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class FragmentManagerStringer implements Stringer<FragmentManager> {
	@Override public @NonNull String toString(FragmentManager fm) {
		int count = fm.getBackStackEntryCount();
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.ROOT, "There are %d entries in the backstack of %s:", count, fm));
		for (int i = 0; i < count; ++i) {
			BackStackEntry entry = fm.getBackStackEntryAt(i);
			sb.append("\n\t#").append(i);
			sb.append(AndroidTools.toString(entry));
		}
		return sb.toString();
	}
}
