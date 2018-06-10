package net.twisterrob.inventory.android;

import android.content.Context;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.multidex.MultiDex;

public class DebugApp extends App {

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		installMultiDex();
	}

	/**
	 * Enable MultiDEX only for debug builds, the production build should shrink below the limit for now.
	 */
	private void installMultiDex() {
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
		try {
			// StrictModeDiskReadViolation: MultiDexExtractor.load -> getZipCrc -> ZipUtil.getZipCrc
			// StrictModeDiskWriteViolation: MultiDexExtractor.load -> lockFile
			MultiDex.install(this);
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}
}
