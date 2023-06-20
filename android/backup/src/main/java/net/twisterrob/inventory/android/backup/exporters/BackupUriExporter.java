package net.twisterrob.inventory.android.backup.exporters;

import java.io.*;

import javax.inject.Inject;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.java.utils.ObjectTools;

public class BackupUriExporter {
	private final BackupStreamExporter exporter;
	private final Context context;

	@VisibleForTesting BackupUriExporter(
			@NonNull Context context,
			@NonNull BackupStreamExporter exporter
	) {
		this.context = context;
		this.exporter = exporter;
	}

	@Inject
	public BackupUriExporter(
			@ApplicationContext @NonNull Context context,
			@NonNull Exporter exporter,
			@NonNull ProgressDispatcher dispatcher
	) {
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
