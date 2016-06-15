package net.twisterrob.android.utils.tostring.stringers.name;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.View;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.Stringer;

import static net.twisterrob.android.utils.tools.AndroidTools.*;

public class ResourceNameStringer implements Stringer<Integer> {
	public static final Stringer<Integer> INSTANCE = new ResourceNameStringer(AndroidTools.getContext());

	private final Context context;
	private final Resources resources;
	public ResourceNameStringer(Context context) {
		this.context = context.getApplicationContext();
		this.resources = context.getResources();
	}
	@Override public @NonNull String toString(Integer object) {
		int id = object;
		return shortenName(getName(id));
	}
	private String shortenName(String name) {
		if (name.startsWith(context.getPackageName())) {
			name = "app" + name.substring(context.getPackageName().length());
		}
		return name;
	}

	/**
	 * -1 will be resolved to {@code "NO_ID"}, 0 is {@code "invalid"}, everything else will be tried to be resolved.
	 * @see View#NO_ID
	 * @see Resources#getIdentifier
	 * @return Fully qualified name of the resource,
	 *         or special values: {@code "View.NO_ID"}, {@code "invalid"}, {@code "not-found::<id>"}
	 */
	private String getName(int id) {
		if (id == View.NO_ID) {
			return "View.NO_ID";
		} else if (id == INVALID_RESOURCE_ID) {
			return "invalid";
		} else {
			try {
				return resources.getResourceName(id);
			} catch (Resources.NotFoundException ignore) {
				return "not-found::" + id;
			}
		}
	}
}
