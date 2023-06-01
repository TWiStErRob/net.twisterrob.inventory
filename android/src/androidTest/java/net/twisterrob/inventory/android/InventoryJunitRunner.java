package net.twisterrob.inventory.android;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.test.platform.io.OutputDirCalculator;

import net.twisterrob.android.test.junit.AndroidJUnitRunner;
import net.twisterrob.android.utils.tools.IOTools;

public class InventoryJunitRunner extends AndroidJUnitRunner { // STOPSHIP remove
	private static final @NonNull Logger LOG = LoggerFactory.getLogger(InventoryJunitRunner.class);

	@Override public void onCreate(Bundle arguments) {
		LOG.warn("onCreate");
		super.onCreate(arguments);
	}

	@Override public void finish(int resultCode, Bundle results) {
		LOG.warn("finish({}, {})", resultCode, results);
		zip();
		super.finish(resultCode, results); // Kills the process!
	}

	private static void zip() {
		File folder = new OutputDirCalculator().getOutputDir();
		try {
			IOTools.zip(new File("/sdcard/test.zip"), false, folder);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}
