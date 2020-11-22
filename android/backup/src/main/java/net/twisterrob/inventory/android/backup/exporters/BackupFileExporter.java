package net.twisterrob.inventory.android.backup.exporters;

import java.io.*;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.backup.*;

public class BackupFileExporter {
	private final BackupStreamExporter exporter;
	private final Context context;

	@VisibleForTesting BackupFileExporter(Context context, BackupStreamExporter exporter) {
		this.context = context;
		this.exporter = exporter;
	}
	public BackupFileExporter(Context context, Exporter exporter, ProgressDispatcher dispatcher) {
		this(context, new BackupStreamExporter(exporter, DBProvider.db(context), dispatcher));
	}

	public Progress exportTo(File file) throws IOException {
		OutputStream os = new FileOutputStream(file);
		Progress progress;
		try {
			progress = exporter.export(os);
		} finally {
			IOTools.ignorantClose(os);
		}
		if (!BuildConfig.DEBUG && progress.failure != null && file != null && !file.delete()) {
			file.deleteOnExit();
			file = null;
		}
		if (file != null && file.exists()) {
			AndroidTools.makeFileDiscoverable(context, file);
		}
		return progress;
	}
}
