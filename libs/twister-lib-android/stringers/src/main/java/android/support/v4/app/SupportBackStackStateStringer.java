package android.support.v4.app;

import javax.annotation.Nonnull;

import net.twisterrob.android.AndroidConstants;
import net.twisterrob.android.utils.tostring.stringers.name.ResourceNameStringer;
import net.twisterrob.java.utils.tostring.*;

/** @see BackStackState#BackStackState(BackStackRecord) */
public class SupportBackStackStateStringer extends Stringer<BackStackState> {
	private static final Stringer<OpsRefStringer.OpsRef> OP_STRINGER = new OpsRefStringer();

	@Override public void toString(@Nonnull ToStringAppender append, BackStackState state) {
		append.identity(state.mIndex, state.mName);
		appendTransition(append, state);
		appendBreadCrumb(append, state);
		appendShared(append, state);
		appendOps(append, state);
	}
	private void appendOps(ToStringAppender append, BackStackState state) {
		int pos = 0;
		while (pos < state.mOps.length - 7) {
			append.item(new OpsRefStringer.OpsRef(state.mOps, pos), OP_STRINGER);
			pos += 7 + state.mOps[pos + 6];
		}
		assert pos == state.mOps.length;
	}
	private void appendTransition(ToStringAppender append, BackStackState state) {
		append.selfDescribingProperty(TransitionCommand.Converter.toString(state.mTransition));
		if (state.mTransitionStyle != AndroidConstants.INVALID_RESOURCE_ID) {
			append.complexProperty("style", state.mTransitionStyle, ResourceNameStringer.INSTANCE);
		} else {
			append.rawProperty("style", "none");
		}
	}
	private void appendShared(ToStringAppender append, BackStackState state) {
		append.beginPropertyGroup("shared");
		if (state.mSharedElementSourceNames != null && state.mSharedElementTargetNames != null) {
			int sharedNamesCount = state.mSharedElementSourceNames.size();
			for (int i = 0; i < sharedNamesCount; i++) {
				String source = state.mSharedElementSourceNames.get(i);
				String target = state.mSharedElementTargetNames.get(i);
				append.item(source, target);
			}
		}
		append.endPropertyGroup();
	}
	private void appendBreadCrumb(ToStringAppender append, BackStackState state) {
		append.beginPropertyGroup("BreadCrumb");
		{
			if (state.mBreadCrumbTitleRes != 0) {
				append.complexProperty("titleRes", state.mBreadCrumbShortTitleRes,
						ResourceNameStringer.INSTANCE);
			} else {
				append.complexProperty("titleText", state.mBreadCrumbTitleText);
			}
			if (state.mBreadCrumbShortTitleRes != 0) {
				append.complexProperty("shortRes", state.mBreadCrumbShortTitleRes,
						ResourceNameStringer.INSTANCE);
			} else {
				append.complexProperty("shortText", state.mBreadCrumbShortTitleText);
			}
		}
		append.endPropertyGroup();
	}

	private static class OpsRefStringer extends Stringer<OpsRefStringer.OpsRef> {
		public static class OpsRef {
			private final int pos;
			private final int cmd;
			private final int index;
			private final int enterAnim;
			private final int exitAnim;
			private final int popEnterAnim;
			private final int popExitAnim;
			private final int N;
			private final int[] removed;
			public OpsRef(int[] mOps, int startPos) {
				int pos = startPos;
				this.pos = startPos;
				this.cmd = mOps[pos++];
				this.index = mOps[pos++];
				this.enterAnim = mOps[pos++];
				this.exitAnim = mOps[pos++];
				this.popEnterAnim = mOps[pos++];
				this.popExitAnim = mOps[pos++];
				this.N = mOps[pos++];
				this.removed = new int[N];
				for (int i = 0; i < N; i++) {
					removed[i] = mOps[pos++];
				}
			}
		}
		@Override public String getType(OpsRef object) {
			return null;
		}
		@Override public void toString(@Nonnull ToStringAppender append, OpsRef op) {
			//noinspection ResourceType TOFIX external annotations?
			String commandString = TransactionOperationCommand.Converter.toString(op.cmd);
			append.identity(op.index, commandString);
			append.complexProperty("removed", op.removed);
			append.beginPropertyGroup("Anim");
			{
				append.complexProperty("enter", op.enterAnim, ResourceNameStringer.INSTANCE);
				append.complexProperty("exit", op.exitAnim, ResourceNameStringer.INSTANCE);
				append.complexProperty("popEnter", op.popEnterAnim, ResourceNameStringer.INSTANCE);
				append.complexProperty("popExit", op.popExitAnim, ResourceNameStringer.INSTANCE);
			}
			append.endPropertyGroup();
		}
	}
}
