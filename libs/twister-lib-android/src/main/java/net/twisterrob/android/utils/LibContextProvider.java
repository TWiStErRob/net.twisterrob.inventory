package net.twisterrob.android.utils;

import android.content.Context;

public class LibContextProvider {
	private static Context s_context;
	public static Context getApplicationContext() {
		return s_context;
	}
	public static void setApplicationContext(Context context) {
		s_context = context;
	}
}
