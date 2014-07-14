package net.twisterrob.inventory.android.activity;

import android.database.*;
import android.widget.Toast;

public abstract class BaseEditActivity extends BaseActivity {
	protected void updateEditedItem() {
		Cursor item = getEditedItem();
		if (item == null) {
			return;
		}
		DatabaseUtils.dumpCursor(item);
		if (item.getCount() == 1) {
			item.moveToFirst();
			fillEditedItem(item);
		} else {
			error(item);
		}
		item.close();
	}

	private void error(Cursor item) {
		String msg;
		if (item.getCount() == 0) {
			msg = "No editable item found!";
		} else {
			msg = "Multiple (" + item.getCount() + ") editable items found!";
		}
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		finish();
	}

	protected abstract Cursor getEditedItem();
	protected abstract void fillEditedItem(Cursor item);
}
