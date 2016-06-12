package android.support.v4.app;

import java.util.Arrays;

import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tools.AndroidTools;

import static net.twisterrob.android.utils.tools.AndroidTools.*;
import static net.twisterrob.java.utils.ArrayTools.*;

public class SupportV4AppAccess {
	private static final FragmentManagerStateStringer FM_STATE_STRINGER = new FragmentManagerStateStringer();
	private static final FragmentStateStringer F_STATE_STRINGER = new FragmentStateStringer();
	private static final BackStackStateStringer BS_STATE_STRINGER = new BackStackStateStringer();

	public static String toString(Object value) {
		if (value instanceof FragmentManagerState) {
			return FM_STATE_STRINGER.toString((FragmentManagerState)value);
		} else if (value instanceof FragmentState) {
			return F_STATE_STRINGER.toString((FragmentState)value);
		} else if (value instanceof BackStackState) {
			return BS_STATE_STRINGER.toString((BackStackState)value);
		}
		return null;
	}
	public static boolean instanceOf(Object value) {
		return value instanceof android.support.v4.app.FragmentManagerState
				|| value instanceof android.support.v4.app.FragmentState
				|| value instanceof android.support.v4.app.BackStackState;
	}

	private static class FragmentManagerStateStringer {
		public @NonNull String toString(FragmentManagerState state) {
			if (state == null) {
				return AndroidTools.NULL;
			}
			StringBuilder sb = new StringBuilder();
			appendArray(sb, "Backstack", state.mBackStack);
			sb.append("\n\t\tAdded (").append(safeLength(state.mAdded)).append("):");
			sb.append(' ').append(Arrays.toString(state.mAdded));
			appendArray(sb, "Fragments", state.mActive);
			return sb.toString();
		}

		@SafeVarargs
		private final <T> void appendArray(StringBuilder sb, String label, T... arr) {
			sb.append("\n\t\t").append(label).append(" (").append(safeLength(arr)).append(")");
			if (arr != null) {
				sb.append(':');
				for (T fState : arr) {
					String fString = SupportV4AppAccess.toString(fState);
					if (fString != null) {
						fString = fString.replace("\n", "\n\t\t\t\t");
					}
					sb.append("\n\t\t\t").append(fString);
				}
			}
		}
	}

	private static class FragmentStateStringer {
		public @NonNull String toString(FragmentState state) {
			if (state == null) {
				return AndroidTools.NULL;
			}
			StringBuilder sb = new StringBuilder();
			appendName(sb, state);
			sb.append(' ');
			sb.append('(');
			{
				appendIdentity(sb, state);
				appendFlags(sb, state);
			}
			sb.append(')');
			appendDetails(sb, state);
			return sb.toString();
		}
		private void appendName(StringBuilder sb, FragmentState state) {
			sb.append('[').append('#').append(state.mIndex).append(']');
			sb.append(' ');
			sb.append('(').append(shortenPackageNames(state.mClassName)).append(')');
			sb.append(toNameString(state.mInstance));
		}

		private void appendIdentity(StringBuilder sb, FragmentState state) {
			sb.append(toNameString(getContext(), state.mFragmentId));
			if (state.mTag != null) {
				sb.append(" as ").append(state.mTag);
			} else {
				sb.append(" in ").append(toNameString(getContext(), state.mContainerId));
			}
		}
		private void appendFlags(StringBuilder sb, FragmentState state) {
			sb.append(',').append(' ').append("layout=").append(state.mFromLayout);
			sb.append(',').append(' ').append("retain=").append(state.mRetainInstance);
			sb.append(',').append(' ').append("detached=").append(state.mDetached);
			appendNullDetails(sb, state);
		}
		private void appendDetails(StringBuilder sb, FragmentState state) {
			if (state.mArguments != null) {
				sb.append('\n');
				sb.append("Arguments: ").append(AndroidTools.toString(state.mArguments));
			}
			if (state.mSavedFragmentState != null) {
				sb.append('\n');
				sb.append("Saved instance state: ").append(AndroidTools.toString(state.mSavedFragmentState));
			}
		}
		private void appendNullDetails(StringBuilder sb, FragmentState state) {
			if (state.mArguments == null) {
				sb.append(',').append(' ').append("args=").append((String)null);
			}
			if (state.mSavedFragmentState == null) {
				sb.append(',').append(' ').append("saved=").append((String)null);
			}
		}
	}

	/** @see BackStackState#BackStackState(BackStackRecord) */
	private static class BackStackStateStringer {
		public @NonNull String toString(BackStackState state) {
			if (state == null) {
				return AndroidTools.NULL;
			}
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
				sb.append(toNameString(getContext(), state.mTransitionStyle));
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
					sb.append(toNameString(getContext(), state.mBreadCrumbTitleRes));
				} else {
					sb.append(state.mBreadCrumbTitleText);
				}
				sb.append(',').append(' ');
				sb.append("short=");
				if (state.mBreadCrumbTitleRes != 0) {
					sb.append(toNameString(getContext(), state.mBreadCrumbShortTitleRes));
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
				sb.append("enter=").append(toNameString(getContext(), enterAnim));
				sb.append(", ");
				sb.append("exit=").append(toNameString(getContext(), exitAnim));
				sb.append(", ");
				sb.append("popEnter=").append(toNameString(getContext(), popEnterAnim));
				sb.append(", ");
				sb.append("popExit=").append(toNameString(getContext(), popExitAnim));
			}
			sb.append(" }");
			return pos;
		}
	}
}
