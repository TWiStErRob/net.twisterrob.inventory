package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.*;
import android.support.v4.app.FragmentManager.BackStackEntry;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;

public class BackStackEntryStringer implements Stringer<BackStackEntry> {
	private final Context context;
	public BackStackEntryStringer(Context context) {
		this.context = context;
	}

	@Override public @NonNull String toString(BackStackEntry entry) {
		CharSequence title = entry.getBreadCrumbTitle();
		CharSequence shortTitle = entry.getBreadCrumbShortTitle();
		String titleRes = resourceIdToString(context, entry.getBreadCrumbTitleRes());
		String shortTitleRes = resourceIdToString(context, entry.getBreadCrumbShortTitleRes());
		return String.format(Locale.ROOT, "%s shortTitle=(%s)%s, title=(%s)%s",
				getName(entry), shortTitleRes, shortTitle, titleRes, title);
	}

	public String getName(BackStackEntry entry) {
		return String.format(Locale.ROOT, "@%d: %s.", entry.getId(), entry.getName());
	}

	private static String resourceIdToString(Context context, @AnyRes int shortTitleRes) {
		if (shortTitleRes != AndroidTools.INVALID_RESOURCE_ID) {
			try {
				return context.getResources().getResourceName(shortTitleRes)
						+ " (" + Integer.toHexString(shortTitleRes) + ")";
			} catch (Resources.NotFoundException ex) {
				return Integer.toHexString(shortTitleRes);
			}
		}
		return null;
	}
}
