package net.twisterrob.inventory.android.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.content.Loader;

import net.twisterrob.inventory.android.content.Loaders;

public class CursorSwapper extends net.twisterrob.android.content.CursorSwapper {
	private final Context context;

	public CursorSwapper(Context context, CursorAdapter adapter) {
		super(adapter);
		this.context = context;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return Loaders.fromID(id).createLoader(context, args);
	}
}