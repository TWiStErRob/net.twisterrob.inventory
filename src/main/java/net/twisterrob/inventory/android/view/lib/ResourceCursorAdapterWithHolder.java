package net.twisterrob.inventory.android.view.lib;

import android.content.Context;
import android.database.*;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.*;

public abstract class ResourceCursorAdapterWithHolder<VH> extends ResourceCursorAdapter {
	public ResourceCursorAdapterWithHolder(Context context, int layout, Cursor c, boolean autoRequery) {
		super(context, layout, c, autoRequery);
	}

	public ResourceCursorAdapterWithHolder(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		view.setTag(createHolder(view));
		return view;
	}

	@Override
	public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newDropDownView(context, cursor, parent);
		view.setTag(createHolder(view));
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		try {
			@SuppressWarnings("unchecked")
			VH holder = (VH)view.getTag();
			bindView(holder, cursor, view);
		} catch (RuntimeException ex) {
			DatabaseUtils.dumpCurrentRow(cursor);
			throw ex;
		}
	}

	protected abstract VH createHolder(View convertView);

	protected abstract void bindView(VH holder, Cursor cursor, View view);
}
