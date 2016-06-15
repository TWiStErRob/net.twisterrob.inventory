package net.twisterrob.android.utils.tostring.stringers.detailed;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;

public class SupportFragmentStringer implements Stringer<Fragment> {
	@Override public @NonNull String toString(Fragment fragment) {
		StringBuilder sb = new StringBuilder();
		sb.append(AndroidTools.toNameString(fragment)).append('[').append(fragment).append(']');
		sb.append(':').append(AndroidTools.toString(fragment.getArguments())).append('\n');
		sb.append("view=").append(fragment.getView()).append('\n');
		sb.append("activity=").append(fragment.getActivity()).append('\n');
		sb.append("context=").append(fragment.getContext()).append('\n');
		sb.append("host=").append(fragment.getHost()).append('\n');
		appendState(sb, fragment.isDetached(), "detached", ", ");
		appendState(sb, fragment.isAdded(), "added", ", ");
		appendState(sb, fragment.isResumed(), "resumed", ", ");
		appendState(sb, fragment.isHidden(), "hidden", ", ");
		appendState(sb, fragment.isVisible(), "visible", "");
		appendState(sb, fragment.isMenuVisible(), "menu visible", ", ");
		appendState(sb, fragment.isInLayout(), "in layout", ", ");
		appendState(sb, fragment.isRemoving(), "removing", ", ");
		return sb.toString();
	}

	private static void appendState(StringBuilder sb, boolean condition, String conditionName, String separator) {
		if (!condition) {
			sb.append("not ");
		}
		sb.append(conditionName);
		sb.append(separator);
	}
}
