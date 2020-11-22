package net.twisterrob.android.utils.tostring.stringers.detailed;

import javax.annotation.Nonnull;

import android.annotation.SuppressLint;

import androidx.fragment.app.Fragment;

import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.java.utils.tostring.*;

public class SupportFragmentStringer extends Stringer<Fragment> {
	@Override public void toString(@Nonnull ToStringAppender append, Fragment fragment) {
		append.identity(fragment, StringerTools.toNameString(fragment));
		append.item("arguments", fragment.getArguments());
		append.item("view", fragment.getView());
		if (fragment.getActivity() == fragment.getHost()) {
			append.item("activity", "-> host");
		} else {
			append.item("activity", fragment.getActivity());
		}
		if (fragment.getContext() == fragment.getHost()) {
			append.item("context", "-> host");
		} else {
			append.item("context", fragment.getContext());
		}
		append.item("host", fragment.getHost());
		append.beginPropertyGroup("state");
		append.booleanProperty(fragment.isDetached(), "detached");
		append.booleanProperty(fragment.isAdded(), "added");
		append.booleanProperty(fragment.isResumed(), "resumed");
		append.booleanProperty(fragment.isHidden(), "hidden");
		append.booleanProperty(fragment.isVisible(), "visible");
		append.booleanProperty(isMenuVisible(fragment), "menu visible");
		append.booleanProperty(fragment.isInLayout(), "in layout");
		append.booleanProperty(fragment.isRemoving(), "removing");
		append.endPropertyGroup();
	}

	@SuppressLint("RestrictedApi")
	private boolean isMenuVisible(Fragment fragment) {
		// use internal API to query state that otherwise impossible to get (sans reflection)
		return fragment.isMenuVisible();
	}
}
