package net.twisterrob.inventory.android.content;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Loaders.LoadersCallbacks;

public abstract class LoadSingleRow extends LoadersCallbacks {
	public LoadSingleRow(Context context) {
		super(context);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		super.onLoadFinished(loader, data);
		//DatabaseTools.dumpCursor(data);
		if (data.getCount() == 1 && data.moveToFirst()) {
			process(data);
		} else {
			processInvalid(data);
		}
		//data.close(); // don't close SimpleCursorLoader (created in Loaders) will do it
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		super.onLoaderReset(loader);
		// no op, we didn't keep any reference to data
	}

	protected void process(@SuppressWarnings("unused") Cursor data) {
		// no op, optional override
	}

	protected void processInvalid(Cursor data) {
		App.toastUser(getInvalidToastMessage(data));
	}

	protected String getInvalidToastMessage(Cursor data) {
		String msg;
		if (data.getCount() == 0) {
			msg = "No data found!";
		} else {
			msg = "Multiple (" + data.getCount() + ") data found!";
		}
		return msg;
	}
}