package net.twisterrob.inventory.android.backup;

import java.io.*;
import java.util.zip.*;

import org.slf4j.*;

import android.content.res.Resources;
import android.support.annotation.VisibleForTesting;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.backup.Importer.ImportProgressHandler;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.Type;

public class BackupZipFileImporter implements ImportProgressHandler, ZipImporter<File> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupZipFileImporter.class);
	private final ProgressDispatcher dispatcher;
	Progress progress;
	private ZipFile zip;
	private final Resources res;
	private Database db;
	private XMLImporter importer;

	public BackupZipFileImporter(Resources res, ProgressDispatcher dispatcher) {
		this(res, dispatcher, App.db(), new XMLImporter(res, App.db()));
	}
	@VisibleForTesting BackupZipFileImporter(Resources res, ProgressDispatcher dispatcher,
			Database db, XMLImporter importer) {
		this.dispatcher = dispatcher;
		this.res = res;
		this.db = db;
		this.importer = importer;
	}

	public void publishStart(int size) {
		progress.done = 0;
		progress.total = size;
		publishProgress();
	}
	public void publishIncrement() {
		progress.done++;
		publishProgress();
	}
	private void publishProgress() {
		dispatcher.dispatchProgress(progress.clone());
	}

	@Override public void warning(String message) {
		LOG.warn("Warning: {}", message);
		progress.warnings.add(message);
	}

	@Override public void error(String message) {
		LOG.warn("Error: {}", message);
		progress.warnings.add(message);
	}

	public Progress importFrom(File file) {
		progress = new Progress();
		zip = null;
		publishStart(-1);
		try {
			db.beginTransaction();

			zip = new ZipFile(file);

			ZipEntry dataFile = zip.getEntry(Paths.BACKUP_DATA_FILENAME);
			if (dataFile != null) {
				//noinspection resource zip is closed in finally
				InputStream stream = zip.getInputStream(dataFile);
				importer.doImport(stream, this);
			} else {
				throw new IllegalArgumentException(String.format("The file %s is not a valid %s backup: %s",
						zip.getName(),
						res.getString(R.string.app_name),
						"missing data file " + Paths.BACKUP_DATA_FILENAME));
			}

			db.setTransactionSuccessful();
		} catch (ZipException ex) {
			progress.failure = new IllegalArgumentException(String.format("%s: %s", file, "invalid zip file"), ex);
		} catch (Throwable ex) {
			progress.failure = ex;
		} finally {
			IOTools.ignorantClose(zip);
			try {
				db.endTransaction();
			} catch (Exception ex) {
				if (progress.failure != null) {
					LOG.warn("Cannot end transaction, exception suppressed by {}", progress.failure, ex);
					warning(String.format("Cannot end transaction: %s", ex));
				} else {
					progress.failure = ex;
				}
			}
		}
		return progress;
	}

	public void importImage(Type type, long id, String name, String image) throws IOException {
		progress.imagesTotal++;
		ZipEntry imageEntry = zip.getEntry(image);
		if (imageEntry != null) {
			InputStream zipImage = zip.getInputStream(imageEntry);
			byte[] imageContents = IOTools.readBytes(zipImage, Math.max(0, imageEntry.getSize()));
			long time = imageEntry.getTime();
			Long dbTime = time != -1? time : null;
			long imageId = db.addImage(imageContents, dbTime);
			switch (type) {
				case Property:
					db.setItemImage(id, imageId);
					break;
				case Room:
					db.setRoomImage(id, imageId);
					break;
				case Item:
					db.setItemImage(id, imageId);
					break;
				default:
					throw new IllegalArgumentException(type + " cannot have images.");
			}
			progress.imagesDone++;
		} else {
			warning(res.getString(R.string.backup_import_invalid_image, name, image));
		}
	}
}
