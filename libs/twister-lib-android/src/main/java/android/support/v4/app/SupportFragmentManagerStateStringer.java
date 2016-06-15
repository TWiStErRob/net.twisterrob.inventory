package android.support.v4.app;

import java.util.Arrays;

import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;

import static net.twisterrob.java.utils.ArrayTools.*;

public class SupportFragmentManagerStateStringer implements Stringer<FragmentManagerState> {
	@Override public @NonNull String toString(FragmentManagerState state) {
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
				String fString = AndroidTools.toString(fState);
				if (fString != null) {
					fString = fString.replace("\n", "\n\t\t\t\t");
				}
				sb.append("\n\t\t\t").append(fString);
			}
		}
	}
}
