package net.twisterrob.android.content.loader;

import android.content.Context;
import android.database.Cursor;

public abstract class SimpleCursorLoader extends AsyncLoader<Cursor> {
	public SimpleCursorLoader(Context context) {
		super(context);
	}

	@Override protected void releaseResources(Cursor data) {
		if (data != null && !data.isClosed()) {
			// CONSIDER calling in the background, long-running DB query may block the UI, because operations are queued
			// e.g. on 2.3.7 go to an Item, and trigger type change dialog, then while loading, go back -> UI is blocked
			// the repro is not viable after materializing Category_Descendant, but the issue still stands
			data.close();
		}
	}
}
