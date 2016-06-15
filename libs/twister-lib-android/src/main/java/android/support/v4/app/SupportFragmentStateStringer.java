package android.support.v4.app;

import android.content.Context;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.android.utils.tostring.stringers.detailed.DefaultStringer;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

public class SupportFragmentStateStringer implements Stringer<FragmentState> {
	private final Context context;
	public SupportFragmentStateStringer(Context context) {
		this.context = context;
	}

	@Override public @NonNull String toString(FragmentState state) {
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
		sb.append('(').append(DefaultStringer.shortenPackageNames(state.mClassName)).append(')');
		sb.append(toNameString(state.mInstance));
	}

	private void appendIdentity(StringBuilder sb, FragmentState state) {
		sb.append(toNameString(context, state.mFragmentId));
		if (state.mTag != null) {
			sb.append(" as ").append(state.mTag);
		} else {
			sb.append(" in ").append(toNameString(context, state.mContainerId));
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
