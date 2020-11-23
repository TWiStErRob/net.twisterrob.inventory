package androidx.fragment.app;

import javax.annotation.Nonnull;

import android.annotation.TargetApi;
import android.os.Build.*;
import android.os.Bundle;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.tostring.stringers.name.*;
import net.twisterrob.java.utils.tostring.*;
import net.twisterrob.java.utils.tostring.stringers.DefaultStringer;

public class SupportFragmentStateStringer extends Stringer<FragmentState> {
	@Override public void toString(@Nonnull ToStringAppender append, FragmentState state) {
	  	append.identity(state.mWho, DefaultStringer.shortenPackageNames(state.mClassName));
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
			fixClassLoader(state.mArguments);
			append.item("Arguments", state.mArguments);
		}
		if (state.mSavedFragmentState != null) {
			fixClassLoader(state.mSavedFragmentState);
			append.item("Saved instance state", state.mSavedFragmentState);
		}
	}

	/**
	 * setClassLoader to mimic proper lifecycle, without this a
	 * <code>android.os.BadParcelableException:
	 * ClassNotFoundException when unmarshalling: android.support.v7.widget.RecyclerView$SavedState</code>
	 * would manifest itself.
	 * @see Fragment#instantiate
	 * @see <a href="https://issuetracker.google.com/issues/37073849">ClassNotFoundException when unmarshalling SavedState</a>
	 */
	@TargetApi(VERSION_CODES.HONEYCOMB)
	private void fixClassLoader(@NonNull Bundle bundle) {
		if (bundle.getClassLoader() == null) {
			bundle.setClassLoader(FragmentState.class.getClassLoader());
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
