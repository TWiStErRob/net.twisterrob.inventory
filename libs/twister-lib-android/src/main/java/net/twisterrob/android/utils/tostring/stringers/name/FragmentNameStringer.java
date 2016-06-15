package net.twisterrob.android.utils.tostring.stringers.name;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.java.utils.ReflectionTools;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

public class FragmentNameStringer implements Stringer<Fragment> {
	public static final Stringer<Fragment> INSTANCE = new FragmentNameStringer();

	@Override public @NonNull String toString(Fragment fragment) {
		return toNameString((Object)fragment)
				+ (fragment != null? "(" + ReflectionTools.get(fragment, "mWho") + ")" : "");
	}
}
