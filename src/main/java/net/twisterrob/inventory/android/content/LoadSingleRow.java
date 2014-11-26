package net.twisterrob.inventory.android.content;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import net.twisterrob.inventory.android.App;

public abstract class LoadSingleRow implements LoaderCallbacks<Cursor> {
	private final Context context;

	public LoadSingleRow(Context context) {
		this.context = context;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return Loaders.fromID(id).createLoader(context, args);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		//DatabaseTools.dumpCursor(data);
		if (data.getCount() == 1) {
			data.moveToFirst();
			process(data);
		} else {
			processInvalid(data);
		}
		//data.close(); // don't close SimpleCursorLoader (created in Loaders) will do it
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		// no op, we didn't keep any reference to data
	}

	protected void process(@SuppressWarnings("unused") Cursor data) {
		// no op, optional override
	}

	protected void processInvalid(Cursor data) {
		App.toast(getInvalidToastMessage(data));
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