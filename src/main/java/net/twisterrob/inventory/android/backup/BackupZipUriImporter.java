package net.twisterrob.inventory.android.backup;

import java.io.*;

import android.content.*;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;

import net.twisterrob.android.utils.tools.IOTools;

public class BackupZipUriImporter {
	private final Context context;
	private final ProgressDispatcher dispatcher;
	private final BackupZipStreamImporter streamImporter;
	private final BackupZipFileImporter fileImporter;

	@VisibleForTesting 
	/*default*/ BackupZipUriImporter(Context context, ProgressDispatcher dispatcher,
			BackupZipStreamImporter streamImporter, BackupZipFileImporter fileImporter) {
		this.context = context;
		this.dispatcher = dispatcher;
		this.streamImporter = streamImporter;
		this.fileImporter = fileImporter;
	}
	public BackupZipUriImporter(Context context, ProgressDispatcher dispatcher) {
		this(context, dispatcher,
				new BackupZipStreamImporter(context.getResources(), dispatcher),
				new BackupZipFileImporter(context.getResources(), dispatcher));
	}

	public Progress importFrom(Uri uri) throws IOException {
		dispatcher.dispatchProgress(new Progress());
		try {
			if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
				return importFile(uri);
			} else {
				return importStream(uri);
			}
		} catch (Throwable ex) {
			return new Progress(ex);
		}
	}

	private Progress importFile(Uri uri) {
		return fileImporter.importFrom(new File(uri.getPath()));
	}

	private Progress importStream(Uri uri) throws FileNotFoundException {
		InputStream stream = null;
		try {
			stream = context.getContentResolver().openInputStream(uri);
			return streamImporter.importFrom(stream);
		} finally {
			IOTools.ignorantClose(stream);
		}
	}
}
