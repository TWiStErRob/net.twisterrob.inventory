package net.twisterrob.inventory.android.backup.exporters;

import java.io.*;

import javax.inject.Inject;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.java.utils.ObjectTools;

public class BackupUriExporter {
	private final @NonNull BackupStreamExporter exporter;
	private final @NonNull ContentResolver contentResolver;

	@Inject
	public BackupUriExporter(
			@NonNull ContentResolver contentResolver,
			@NonNull BackupStreamExporter exporter
	) {
		this.contentResolver = contentResolver;
		this.exporter = exporter;
	}

	public Progress exportTo(Uri uri) throws IOException {
		OutputStream stream;
		try {
			stream = contentResolver.openOutputStream(uri);
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
