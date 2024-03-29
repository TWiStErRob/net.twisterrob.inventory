package net.twisterrob.inventory.android.backup.importers;

import javax.inject.Inject;

import org.slf4j.*;

import androidx.annotation.NonNull;

import net.twisterrob.inventory.android.backup.ImportProgressHandler;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.java.utils.ObjectTools;

public class BackupTransactingImporter<T> implements ZipImporter<T> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupTransactingImporter.class);

	private final @NonNull Database db;
	private final @NonNull ImportProgressHandler progress;
	private final @NonNull ZipImporter<T> importer;

	@Inject
	public BackupTransactingImporter(
			@NonNull Database db,
			@NonNull ImportProgressHandler progress,
			@NonNull ZipImporter<T> importer
	) {
		this.db = ObjectTools.checkNotNull(db);
		this.progress = ObjectTools.checkNotNull(progress);
		this.importer = ObjectTools.checkNotNull(importer);
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
