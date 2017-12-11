package net.twisterrob.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;

/** @deprecated find other ways! */
@Deprecated
public class LibContextProvider {
	@SuppressLint("StaticFieldLeak")
	private static Context s_context;
	public static Context getApplicationContext() {
		return s_context;
	}
	public static void setApplicationContext(Context context) {
		s_context = context;
	}
}
