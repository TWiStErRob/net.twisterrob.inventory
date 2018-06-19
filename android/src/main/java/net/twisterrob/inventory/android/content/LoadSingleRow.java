package net.twisterrob.inventory.android.content;

import android.database.Cursor;
import android.support.annotation.*;
import android.support.v4.content.Loader;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.InventoryLoader.LoadersCallbacksAdapter;

public abstract class LoadSingleRow extends LoadersCallbacksAdapter {
	@Override public void postOnLoadFinished(Loader<Cursor> loader,
			Cursor data) {
		//DatabaseTools.dumpCursor(data);
		if (data != null && data.getCount() == 1 && data.moveToFirst()) {
			process(data);
		} else {
			processInvalid(data);
		}
		//data.close(); // don't close SimpleCursorLoader (created in Loaders) will do it
	}

	@Override public void postOnLoaderReset(Loader<Cursor> loader) {
		// no op, we didn't keep any reference to data
	}

	protected void process(@SuppressWarnings("unused") @NonNull Cursor data) {
		// no op, optional override
	}

	protected void processInvalid(@Nullable Cursor data) {
		App.toastUser(getInvalidToastMessage(data));
	}

	protected String getInvalidToastMessage(Cursor data) {
		String msg;
		if (data == null || data.getCount() == 0) {
			msg = "No data found!";
		} else {
			msg = "Multiple (" + data.getCount() + ") data found!";
		}
		return msg;
	}
}
