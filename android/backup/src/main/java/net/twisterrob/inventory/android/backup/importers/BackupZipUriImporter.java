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
	private final @NonNull BackupZipFileImporter fileImporter;

	@Inject
	public BackupZipUriImporter(
			@ApplicationContext
			@NonNull ContentResolver contentResolver,
			@NonNull BackupZipStreamImporter streamImporter,
			@NonNull BackupZipFileImporter fileImporter
	) {
		this.contentResolver = contentResolver;
		this.streamImporter = streamImporter;
		this.fileImporter = fileImporter;
	}

	@Override public void importFrom(Uri uri) throws Exception {
		try {
			if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
				importFile(uri);
			} else {
				importStream(uri);
			}
		} catch (Exception ex) {
			IllegalArgumentException cause = new IllegalArgumentException("Source URI: " + uri);
			//noinspection ThrowableNotThrown
			ObjectTools.getRootCause(ex).initCause(cause);
			throw ex;
		}
	}

	private void importFile(Uri uri) throws Exception {
		fileImporter.importFrom(new File(uri.getPath()));
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
