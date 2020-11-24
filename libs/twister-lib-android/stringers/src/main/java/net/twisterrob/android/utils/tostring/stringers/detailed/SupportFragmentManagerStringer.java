package net.twisterrob.android.utils.tostring.stringers.detailed;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.tostring.*;

@DebugHelper
public class SupportFragmentManagerStringer extends Stringer<FragmentManager> {
	@Override public void toString(@NonNull ToStringAppender append, FragmentManager fm) {
		int count = fm.getBackStackEntryCount();
		append.beginSizedList("FragmentManager backstack", count);
		for (int i = 0; i < count; ++i) {
			append.item(i, fm.getBackStackEntryAt(i));
		}
		append.endSizedList();
	}
}
