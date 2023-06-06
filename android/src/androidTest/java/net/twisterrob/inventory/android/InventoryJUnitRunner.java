package net.twisterrob.inventory.android;

import java.io.File;
import java.io.IOException;

import android.os.Build;
import android.os.Bundle;

import androidx.test.platform.io.OutputDirCalculator;

import net.twisterrob.android.test.junit.AndroidJUnitRunner;
import net.twisterrob.android.utils.tools.IOTools;

public class InventoryJUnitRunner extends AndroidJUnitRunner {
	@Override public void finish(int resultCode, Bundle results) {
		zip();
		super.finish(resultCode, results); // Kills the process!
	}

	// TODEL once AGP UTP is used.
	private static void zip() {
		if (Build.VERSION_CODES.Q < Build.VERSION.SDK_INT) {
			// Not supported, because of https://developer.android.com/about/versions/11/privacy/storage
			return;
		}
		File folder = new OutputDirCalculator().getOutputDir();
		try {
			IOTools.zip(new File("/sdcard/test.zip"), false, folder);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}
