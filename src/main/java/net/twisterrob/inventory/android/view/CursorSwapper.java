package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

import net.twisterrob.inventory.android.db.Loaders;

public class CursorSwapper extends net.twisterrob.android.db.CursorSwapper {
	public CursorSwapper(Context context, CursorAdapter adapter) {
		super(context, adapter);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return Loaders.fromID(id).createLoader(context, bundle);
	}
}