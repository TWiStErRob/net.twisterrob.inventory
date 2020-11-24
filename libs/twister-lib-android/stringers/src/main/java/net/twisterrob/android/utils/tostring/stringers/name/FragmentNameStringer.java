package net.twisterrob.android.utils.tostring.stringers.name;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.twisterrob.java.utils.*;
import net.twisterrob.java.utils.tostring.*;

public class FragmentNameStringer extends Stringer<Fragment> {
	public static final Stringer<Fragment> INSTANCE = new FragmentNameStringer();

	@Override public void toString(@NonNull ToStringAppender append, Fragment fragment) {
		if (fragment == null) {
			append.selfDescribingProperty(StringTools.NULL_STRING);
			return;
		}
		append.identity(StringTools.hashString(fragment), ReflectionTools.get(fragment, "mWho"));
	}
}
