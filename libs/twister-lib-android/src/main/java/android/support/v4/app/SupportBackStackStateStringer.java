package android.support.v4.app;

import java.util.Arrays;

import android.content.Context;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tostring.Stringer;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

/** @see BackStackState#BackStackState(BackStackRecord) */
public class SupportBackStackStateStringer implements Stringer<BackStackState> {
	private final Context context;
	public SupportBackStackStateStringer(Context context) {
		this.context = context;
	}

	@Override public @NonNull String toString(BackStackState state) {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append('#').append(state.mIndex).append(']');
		sb.append(' ');
		sb.append(state.mName);
		sb.append(' ');
		appendTransition(sb, state);
		sb.append(' ');
		sb.append('(');
		{
			appendBreadCrumb(sb, state);
			sb.append(',').append(' ');
			appendShared(sb, state);
		}
		sb.append(')');
		appendOps(sb, state);
		return sb.toString();
	}
	private void appendOps(StringBuilder sb, BackStackState state) {
		int pos = 0;
		while (pos < state.mOps.length) {
			sb.append("\n");
			pos = appendOpString(sb, state.mOps, pos);
		}
	}
	private void appendTransition(StringBuilder sb, BackStackState state) {
		sb.append(TransitionCommand.Converter.toString(state.mTransition));
		sb.append("(style=");
		if (state.mTransitionStyle != 0) {
			sb.append(toNameString(context, state.mTransitionStyle));
		} else {
			sb.append("none");
		}
		sb.append(')');
	}
	private void appendShared(StringBuilder sb, BackStackState state) {
		sb.append("shared=[");
		if (state.mSharedElementSourceNames != null && state.mSharedElementTargetNames != null) {
			int sharedNamesCount = state.mSharedElementSourceNames.size();
			for (int i = 0; i < sharedNamesCount; i++) {
				if (0 < i) {
					sb.append(", ");
				}
				sb.append(state.mSharedElementSourceNames.get(i));
				sb.append("->");
				sb.append(state.mSharedElementTargetNames.get(i));
			}
		}
		sb.append("]");
	}
	private void appendBreadCrumb(StringBuilder sb, BackStackState state) {
		sb.append("BreadCrumb: { ");
		{
			sb.append("title=");
			if (state.mBreadCrumbTitleRes != 0) {
				sb.append(toNameString(context, state.mBreadCrumbTitleRes));
			} else {
				sb.append(state.mBreadCrumbTitleText);
			}
			sb.append(',').append(' ');
			sb.append("short=");
			if (state.mBreadCrumbTitleRes != 0) {
				sb.append(toNameString(context, state.mBreadCrumbShortTitleRes));
			} else {
				sb.append(state.mBreadCrumbShortTitleText);
			}
		}
		sb.append(" }");
	}

	private int appendOpString(StringBuilder sb, int[] mOps, int pos) {
		int cmd = mOps[pos++];
		int index = mOps[pos++];
		int enterAnim = mOps[pos++];
		int exitAnim = mOps[pos++];
		int popEnterAnim = mOps[pos++];
		int popExitAnim = mOps[pos++];
		int N = mOps[pos++];
		int[] removed = new int[N];
		for (int i = 0; i < N; i++) {
			removed[i] = mOps[pos++];
		}

		//noinspection ResourceType TOFIX external annotations?
		sb.append(TransactionOperationCommand.Converter.toString(cmd));
		sb.append(' ').append('@').append(index);
		sb.append(' ').append("removed=").append(Arrays.toString(removed));
		sb.append(' ').append("Anim: { ");
		{
			sb.append("enter=").append(toNameString(context, enterAnim));
			sb.append(", ");
			sb.append("exit=").append(toNameString(context, exitAnim));
			sb.append(", ");
			sb.append("popEnter=").append(toNameString(context, popEnterAnim));
			sb.append(", ");
			sb.append("popExit=").append(toNameString(context, popExitAnim));
		}
		sb.append(" }");
		return pos;
	}
}
