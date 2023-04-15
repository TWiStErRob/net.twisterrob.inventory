package net.twisterrob.inventory.android.backup.exporters;

import java.io.*;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.java.utils.ObjectTools;

public class BackupUriExporter {
	private final BackupStreamExporter exporter;
	private final Context context;

	@VisibleForTesting BackupUriExporter(Context context, BackupStreamExporter exporter) {
		this.context = context;
		this.exporter = exporter;
	}
	public BackupUriExporter(Context context, Exporter exporter, ProgressDispatcher dispatcher) {
		this(context, new BackupStreamExporter(exporter, DBProvider.db(context), dispatcher));
	}

	public Progress exportTo(Uri uri) throws IOException {
		OutputStream stream;
		try {
			stream = context.getContentResolver().openOutputStream(uri);
		} catch (Exception ex) {
			IllegalArgumentException cause = new IllegalArgumentException("Target URI: " + uri);
			//noinspection ThrowableNotThrown
			ObjectTools.getRootCause(ex).initCause(cause);
			throw ex;
		}
		try {
			return exporter.export(stream);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}
}
