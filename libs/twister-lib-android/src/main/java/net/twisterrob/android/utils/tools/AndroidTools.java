package net.twisterrob.android.utils.tools;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.widget.CursorAdapter;
import android.widget.AdapterView;

public abstract class AndroidTools {
	private AndroidTools() {
		// static class
	}

	public static boolean hasPermission(Context context, String permission) {
		PackageManager packageManager = context.getPackageManager();
		// alternative: context.checkCallingOrSelfPermission
		int permissionResult = packageManager.checkPermission(permission, context.getPackageName());
		return permissionResult == PackageManager.PERMISSION_GRANTED;
	}

	public static void selectByID(AdapterView<?> view, long id) {
		CursorAdapter myAdapter = (CursorAdapter)view.getAdapter();
		for (int i = 0, n = myAdapter.getCount(); i < n; i++) {
			if (myAdapter.getItemId(i) == id) {
				view.setSelection(i);
				break;
			}
		}
	}
}
