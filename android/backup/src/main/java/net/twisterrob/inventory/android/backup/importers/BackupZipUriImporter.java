package net.twisterrob.inventory.android.backup.importers;

import java.io.*;

import javax.inject.Inject;

import android.content.*;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.java.utils.ObjectTools;

public class BackupZipUriImporter implements ZipImporter<Uri> {
	private final Context context;
	private final BackupZipStreamImporter streamImporter;
	private final BackupZipFileImporter fileImporter;

	@Inject
	public BackupZipUriImporter(@ApplicationContext Context context, ImportProgressHandler progress) {
		this(context, // STOPSHIP
				new BackupZipStreamImporter(context.getResources(), DBProvider.db(context), progress),
				new BackupZipFileImporter(context.getResources(), DBProvider.db(context), progress)
		);
	}

	@VisibleForTesting BackupZipUriImporter(Context context,
			BackupZipStreamImporter streamImporter, BackupZipFileImporter fileImporter) {
		this.context = context;
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
		InputStream stream = context.getContentResolver().openInputStream(uri);
		try {
			streamImporter.importFrom(stream);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}
}
