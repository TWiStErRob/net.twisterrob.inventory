package net.twisterrob.inventory.android.backup.exporters;

import java.io.*;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.backup.*;

public class BackupDirExporter {
	private final BackupFileExporter exporter;

	@VisibleForTesting BackupDirExporter(BackupFileExporter exporter) {
		this.exporter = exporter;
	}

	public BackupDirExporter(Context context, Exporter exporter, ProgressDispatcher dispatcher) {
		this(new BackupFileExporter(context, exporter, dispatcher));
	}

	public Progress exportTo(File dir) throws IOException {
		File file = Paths.getExportFile(dir);
		return exporter.exportTo(file);
	}
}
