package net.twisterrob.android.utils.tostring.stringers.detailed;

import javax.annotation.Nonnull;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.tostring.*;

@SuppressWarnings("rawtypes")
@DebugHelper
public class SparseArrayStringer extends Stringer<SparseArray> {
	/** @see View#toString() */
	private static final int RESOURCE_ID_MASK = 0xff000000;

	private final Context context;
	public SparseArrayStringer(Context context) {
		this.context = context;
	}

	@Override public String getType(SparseArray object) {
		return null;
	}
	@Override public void toString(@Nonnull ToStringAppender append, SparseArray array) {
		append.beginSizedList(array, array.size());
		for (int index = 0, size = array.size(); index < size; ++index) {
			int key = array.keyAt(index);
			Object value = array.valueAt(index);
			if ((key & RESOURCE_ID_MASK) != 0) {
				append.item(AndroidTools.toNameString(context, key), value);
			} else {
				append.item(key, value);
			}
		}
		append.endSizedList();
	}
}
