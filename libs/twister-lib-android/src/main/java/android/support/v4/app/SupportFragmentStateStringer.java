package android.support.v4.app;

import javax.annotation.Nonnull;

import net.twisterrob.android.utils.tostring.stringers.name.*;
import net.twisterrob.java.utils.tostring.*;
import net.twisterrob.java.utils.tostring.stringers.DefaultStringer;

public class SupportFragmentStateStringer extends Stringer<FragmentState> {
	@Override public void toString(@Nonnull ToStringAppender append, FragmentState state) {
		append.identity(state.mIndex, DefaultStringer.shortenPackageNames(state.mClassName));
		append.complexProperty("instance", state.mInstance, FragmentNameStringer.INSTANCE);
		append.beginPropertyGroup(null);
		{
			appendIdentity(append, state);
			appendFlags(append, state);
		}
		append.endPropertyGroup();
		appendDetails(append, state);
	}

	private void appendIdentity(ToStringAppender append, FragmentState state) {
		append.complexProperty("id", state.mFragmentId, ResourceNameStringer.INSTANCE);
		if (state.mTag != null) {
			append.rawProperty("tag", state.mTag);
		} else {
			append.complexProperty("container", state.mContainerId, ResourceNameStringer.INSTANCE);
		}
	}

	private void appendFlags(ToStringAppender append, FragmentState state) {
		append.booleanProperty(state.mFromLayout, "from layout");
		append.booleanProperty(state.mRetainInstance, "retained");
		append.booleanProperty(state.mDetached, "detached", "attached");
		appendNullDetails(append, state);
	}

	private void appendDetails(ToStringAppender append, FragmentState state) {
		if (state.mArguments != null) {
			append.item("Arguments", state.mArguments);
		}
		if (state.mSavedFragmentState != null) {
			append.item("Saved instance state", state.mSavedFragmentState);
		}
	}

	private void appendNullDetails(ToStringAppender append, FragmentState state) {
		if (state.mArguments == null) {
			append.rawProperty("args", null);
		}
		if (state.mSavedFragmentState == null) {
			append.rawProperty("saved", null);
		}
	}
}
