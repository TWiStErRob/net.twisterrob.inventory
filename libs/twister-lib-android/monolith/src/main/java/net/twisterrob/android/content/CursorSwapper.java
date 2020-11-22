package net.twisterrob.android.content;

import android.database.Cursor;

import androidx.annotation.*;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

public abstract class CursorSwapper implements LoaderCallbacks<Cursor> {
	protected final @NonNull CursorAdapter adapter;

	public CursorSwapper(@NonNull CursorAdapter adapter) {
		//noinspection ConstantConditions just make sure.
		if (adapter == null) {
			throw new IllegalArgumentException("Adapter cannot be null");
		}
		this.adapter = adapter;
	}

	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
		updateAdapter(null);
	}

	public void onLoadFinished(@NonNull Loader<Cursor> loader, @Nullable Cursor data) {
		updateAdapter(data);
	}

	protected void updateAdapter(@Nullable Cursor data) {
		adapter.swapCursor(data);
	}
}
