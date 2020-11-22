package net.twisterrob.android.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.*;
import android.os.Build.*;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.ResourceCursorAdapter;

public abstract class ResourceCursorAdapterWithHolder<VH> extends ResourceCursorAdapter {
	public static boolean devMode = false;

	/** @deprecated {@link ResourceCursorAdapter#ResourceCursorAdapter(android.content.Context, int, android.database.Cursor, boolean)} */
	@SuppressWarnings("deprecation")
	@Deprecated
	public ResourceCursorAdapterWithHolder(@NonNull Context context, int layout, Cursor c, boolean autoRequery) {
		super(context, layout, c, autoRequery);
	}

	public ResourceCursorAdapterWithHolder(@NonNull Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override public View newView(@NonNull Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		view.setTag(createHolder(view));
		return view;
	}

	@Override public View newDropDownView(@NonNull Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newDropDownView(context, cursor, parent);
		view.setTag(createHolder(view));
		return view;
	}

	@TargetApi(VERSION_CODES.KITKAT)
	@Override public void bindView(@NonNull View view, @NonNull Context context, Cursor cursor) {
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

	protected abstract @NonNull VH createHolder(@NonNull View convertView);

	protected abstract void bindView(@NonNull VH holder, @NonNull Cursor cursor, @NonNull View view);
}
