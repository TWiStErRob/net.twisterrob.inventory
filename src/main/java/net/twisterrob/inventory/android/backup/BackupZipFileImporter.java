package net.twisterrob.inventory.android.backup;

import java.io.*;
import java.util.zip.*;

import org.slf4j.*;

import android.content.res.Resources;
import android.support.annotation.VisibleForTesting;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.backup.Importer.ImportImageGetter;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.Type;
import net.twisterrob.java.utils.ObjectTools;

public class BackupZipFileImporter implements ImportImageGetter, ZipImporter<File> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupZipFileImporter.class);
	private final ImportProgressHandler progress;
	private ZipFile zip;
	private final Resources res;
	private Database db;
	private XMLImporter importer;

	public BackupZipFileImporter(Resources res, ImportProgressHandler progress) {
		this(res, App.db(), new XMLImporter(res, App.db()), progress);
	}
	@VisibleForTesting BackupZipFileImporter(Resources res,
			Database db, XMLImporter importer, ImportProgressHandler progress) {
		this.progress = ObjectTools.checkNotNull(progress);
		this.res = ObjectTools.checkNotNull(res);
		this.db = ObjectTools.checkNotNull(db);
		this.importer = ObjectTools.checkNotNull(importer);
	}

	@Override public void importFrom(File file) throws Exception {
		zip = null;
		try {
			LOG.trace("Starting import from {}", file.getAbsolutePath());
			zip = new ZipFile(file);

			ZipEntry dataFile = zip.getEntry(Paths.BACKUP_DATA_FILENAME);
			if (dataFile != null) {
				//noinspection resource zip is closed in finally
				InputStream stream = zip.getInputStream(dataFile);
				importer.doImport(stream, progress, this);
			} else {
				throw new IllegalArgumentException(String.format("The file %s is not a valid %s backup: %s",
						zip.getName(),
						res.getString(R.string.app_name),
						"missing data file " + Paths.BACKUP_DATA_FILENAME));
			}
		} catch (ZipException ex) {
			throw new IllegalArgumentException(String.format("Invalid zip file: %s", file), ex);
		} finally {
			IOTools.ignorantClose(zip);
		}
	}

	@Override public void importImage(Type type, long id, String name, String image) throws IOException {
		progress.imageTotalIncrement();
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
			progress.imageIncrement();
		} else {
			progress.warning(res.getString(R.string.backup_import_invalid_image, name, image));
		}
	}
}
