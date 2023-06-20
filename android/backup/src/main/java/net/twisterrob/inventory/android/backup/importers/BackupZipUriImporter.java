package net.twisterrob.inventory.android.backup.importers;

import java.io.*;

import javax.inject.Inject;

import android.content.*;
import android.net.Uri;

import androidx.annotation.NonNull;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.java.utils.ObjectTools;

public class BackupZipUriImporter implements ZipImporter<Uri> {
	private final @NonNull ContentResolver contentResolver;
	private final @NonNull BackupZipStreamImporter streamImporter;

	@Inject
	public BackupZipUriImporter(
			@ApplicationContext
			@NonNull ContentResolver contentResolver,
			@NonNull BackupZipStreamImporter streamImporter
	) {
		this.contentResolver = contentResolver;
		this.streamImporter = streamImporter;
	}

	@Override public void importFrom(Uri uri) throws Exception {
		try {
			importStream(uri);
		} catch (Exception ex) {
			IllegalArgumentException cause = new IllegalArgumentException("Source URI: " + uri);
			//noinspection ThrowableNotThrown
			ObjectTools.getRootCause(ex).initCause(cause);
			throw ex;
		}
	}

	private void importStream(Uri uri) throws Exception {
		InputStream stream = contentResolver.openInputStream(uri);
		try {
			streamImporter.importFrom(stream);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}
}
