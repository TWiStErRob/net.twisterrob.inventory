package net.twisterrob.android.utils.tostring.stringers.detailed;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build.*;
import android.support.annotation.NonNull;

import net.twisterrob.android.annotation.IntentFlags;
import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.java.annotations.DebugHelper;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

@DebugHelper
@TargetApi(VERSION_CODES.JELLY_BEAN)
@SuppressWarnings({"ConstantConditions", "UnusedAssignment"}) // just so all lines look similar
public class IntentStringer implements Stringer<Intent> {
	@Override public @NonNull String toString(Intent intent) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		first = append(sb, "pkg=", intent.getPackage(), "", first);
		first = append(sb, "cmp=", intent.getComponent(), "", first);

		first = append(sb, "xtra={", toShortString(intent.getExtras()), "}", first);
		if (VERSION_CODES.JELLY_BEAN <= VERSION.SDK_INT) {
			first = append(sb, "clip={", intent.getClipData(), "}", first);
		}

		first = append(sb, "dat=", intent.getData(), "", first);
		first = append(sb, "typ=", intent.getType(), "", first);

		first = append(sb, "act=", intent.getAction(), "", first);
		first = append(sb, "cat=", intent.getCategories(), "", first);
		//noinspection ResourceType TOFIX external annotations to Intent#getFlags?
		first = append(sb, "flg=", IntentFlags.Converter.toString(intent.getFlags(), null), "", first);

		first = append(sb, "bnds=", intent.getSourceBounds(), "", first);
		return sb.toString();
	}

	private static boolean append(StringBuilder b, String prefix, Object data, String suffix,
			boolean first) {
		if (data != null) {
			if (!first) {
				b.append(' ');
			}
			b.append(prefix).append(data).append(suffix);
			first = false;
		}
		return first;
	}
}
