package net.twisterrob.android.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.*;
import android.os.Build.*;
import android.view.*;

import androidx.cursoradapter.widget.ResourceCursorAdapter;

public abstract class ResourceCursorAdapterWithHolder<VH> extends ResourceCursorAdapter {
	public static boolean devMode = false;

	/** @deprecated {@link ResourceCursorAdapter#ResourceCursorAdapter(android.content.Context, int, android.database.Cursor, boolean)} */
	@SuppressWarnings("deprecation")
	@Deprecated
	public ResourceCursorAdapterWithHolder(Context context, int layout, Cursor c, boolean autoRequery) {
		super(context, layout, c, autoRequery);
	}

	public ResourceCursorAdapterWithHolder(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		view.setTag(createHolder(view));
		return view;
	}

	@Override public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newDropDownView(context, cursor, parent);
		view.setTag(createHolder(view));
		return view;
	}

	@TargetApi(VERSION_CODES.KITKAT)
	@Override public void bindView(View view, Context context, Cursor cursor) {
		try {
			@SuppressWarnings("unchecked")
			VH holder = (VH)view.getTag();
			bindView(holder, cursor, view);
		} catch (RuntimeException ex) {
			if (devMode) {
				try {
					DatabaseUtils.dumpCurrentRow(cursor);
				} catch (RuntimeException e) {
					if (VERSION_CODES.KITKAT <= VERSION.SDK_INT) {
						ex.addSuppressed(e);
					}
				}
			}
			throw ex;
		}
	}

	protected abstract VH createHolder(View convertView);

	protected abstract void bindView(VH holder, Cursor cursor, View view);
}
