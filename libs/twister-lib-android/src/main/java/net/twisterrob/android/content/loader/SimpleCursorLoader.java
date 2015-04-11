package net.twisterrob.android.content.loader;

import android.content.Context;
import android.database.Cursor;

public abstract class SimpleCursorLoader extends AsyncLoader<Cursor> {
	public SimpleCursorLoader(Context context) {
		super(context);
	}

	@Override protected void releaseResources(Cursor data) {
		if (data != null && !data.isClosed()) {
			data.close();
		}
	}
}
