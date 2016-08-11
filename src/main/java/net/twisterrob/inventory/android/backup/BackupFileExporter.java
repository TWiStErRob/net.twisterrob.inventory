package net.twisterrob.inventory.android.backup;

import java.io.*;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.BuildConfig;
import net.twisterrob.inventory.android.backup.BackupStreamExporter.ProgressDispatcher;
import net.twisterrob.inventory.android.backup.Exporter.ExportCallbacks.Progress;

public class BackupFileExporter {
	private final BackupStreamExporter exporter;
	private final Context context;

	@VisibleForTesting 
	/*default*/ BackupFileExporter(Context context, BackupStreamExporter exporter) {
		this.context = context;
		this.exporter = exporter;
	}
	public BackupFileExporter(Context context, Exporter exporter, ProgressDispatcher dispatcher) {
		this(context, new BackupStreamExporter(exporter, dispatcher));
	}

	public Progress exportTo(File file) throws IOException {
		OutputStream os = new FileOutputStream(file);
		Progress progress = exporter.export(os);
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
