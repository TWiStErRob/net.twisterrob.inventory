package net.twisterrob.inventory.android.backup.importers;

import org.slf4j.*;

import android.support.annotation.*;

import net.twisterrob.inventory.android.backup.ImportProgressHandler;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.java.utils.ObjectTools;

public class BackupTransactingImporter<T> implements ZipImporter<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupTransactingImporter.class);

	private final ImportProgressHandler progress;
	private final @NonNull Database db;
	private final ZipImporter<T> importer;

	public BackupTransactingImporter(ZipImporter<T> importer, ImportProgressHandler progress, Database db) {
		this.importer = ObjectTools.checkNotNull(importer);
		this.progress = ObjectTools.checkNotNull(progress);
		this.db = ObjectTools.checkNotNull(db);
	}

	@Override public void importFrom(T input) throws Exception {
		boolean thrown = false;
		try {
			db.beginTransaction();
			importer.importFrom(input);
			db.setTransactionSuccessful();
		} catch (Throwable ex) {
			thrown = true;
			throw ex;
		} finally {
			if (!thrown) {
				db.endTransaction();
			} else {
				try {
					db.endTransaction();
				} catch (Exception ex) {
					LOG.warn("Cannot end transaction", ex);
					progress.error(ex.toString());
				}
			}
		}
	}
}
