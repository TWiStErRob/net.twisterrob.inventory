package net.twisterrob.inventory.android.backup.exporters;

import java.io.*;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.backup.*;

public class BackupUriExporter {
	private final BackupStreamExporter exporter;
	private final Context context;

	@VisibleForTesting BackupUriExporter(Context context, BackupStreamExporter exporter) {
		this.context = context;
		this.exporter = exporter;
	}
	public BackupUriExporter(Context context, Exporter exporter, ProgressDispatcher dispatcher) {
		this(context, new BackupStreamExporter(exporter, dispatcher));
	}

	public Progress exportTo(Uri uri) throws IOException {
		OutputStream stream = context.getContentResolver().openOutputStream(uri);
		try {
			return exporter.export(stream);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}
}
