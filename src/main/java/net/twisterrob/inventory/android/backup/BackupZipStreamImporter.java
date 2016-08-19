package net.twisterrob.inventory.android.backup;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.*;

import org.slf4j.*;

import android.content.res.Resources;
import android.support.annotation.VisibleForTesting;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.backup.Importer.ImportProgressHandler;
import net.twisterrob.inventory.android.backup.Progress.Phase;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.Type;
import net.twisterrob.java.io.NonClosableStream;

// TODO extract DB transaction and progress handling
public class BackupZipStreamImporter implements ImportProgressHandler {
	private static final Logger LOG = LoggerFactory.getLogger(BackupZipStreamImporter.class);

	private Progress progress;
	private Images images;

	private final Resources res;
	private final ProgressDispatcher dispatcher;
	private final XMLImporter importer;
	private final Database db;

	public BackupZipStreamImporter(Resources res, ProgressDispatcher dispatcher) {
		this(res, dispatcher, new XMLImporter(res, App.db()), App.db());
	}
	@VisibleForTesting BackupZipStreamImporter(Resources res, ProgressDispatcher dispatcher,
			XMLImporter importer, Database db) {
		this.res = res;
		this.dispatcher = dispatcher;
		this.importer = importer;
		this.db = db;
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

	public Progress importFrom(InputStream source) {
		progress = new Progress();
		publishProgress();
		images = new Images(db);
		ZipInputStream zip = null;
		try {
			db.beginTransaction();
			Pattern image = Pattern.compile("(property|room|item)_(\\d+)_(\\d{8}_\\d{6}).jpg");

			zip = new ZipInputStream(source);
			boolean seenEntry = false;
			NonClosableStream nonClosableZip = new NonClosableStream(zip);
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				LOG.trace("Found ZIP entry {}", AndroidTools.toString(entry));
				if (Constants.Paths.BACKUP_DATA_FILENAME.equals(entry.getName())) {
					seenEntry = true;
					LOG.trace("Importing XML data: {}", entry.getName());
					progress.phase = Phase.Data;
					importer.doImport(nonClosableZip, this);
					progress.imagesTotal = progress.imagesDone + images.reconcile.size();
					publishProgress();
					LOG.trace("Finished importing XML data");
				} else if (image.matcher(entry.getName()).matches()) {
					LOG.trace("Importing image file: {}", entry.getName());
					progress.phase = Phase.Images;
					images.foundImageFile(entry, nonClosableZip, seenEntry);
					publishProgress();
					LOG.trace("Finished importing image file");
				} else {
					LOG.debug("Skipping unknown file: {}", entry.getName());
				}
			}
			images.cleanup();
			if (!seenEntry) {
				throw new IllegalArgumentException(String.format("The import is not a valid %s backup: %s",
						res.getString(R.string.app_name), "missing data file " + Paths.BACKUP_DATA_FILENAME));
			}

			db.setTransactionSuccessful();
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

	public void importImage(Type type, long id, String name, String imageName) throws IOException {
		images.foundImageReference(type, id, name, imageName);
	}

	private static class Belonging {
		final Type type;
		final long id;
		final String name;
		private final String image;

		public Belonging(Type type, long id, String name, String image) {
			this.type = type;
			this.id = id;
			this.name = name;
			this.image = image;
		}
	}

	private static class Image {
		final long imageId;
		final ZipEntry entry;

		private Image(ZipEntry imageEntry, long imageId) throws IOException {
			this.entry = imageEntry;
			this.imageId = imageId;
		}
	}

	private class Images {
		private final Database db;
		Map<String, Object> reconcile = new HashMap<>();
		public Images(Database db) {
			this.db = db;
		}

		public void foundImageFile(ZipEntry imageEntry, InputStream stream, boolean seenXML) throws IOException {
			String imageName = imageEntry.getName();
			String imageKey = imageEntry.getName();
			Object existing = reconcile.get(imageKey);
			if (existing == null) {
				if (!seenXML) {
					LOG.trace("Creating image '{}', in hopes of future reference.", imageName);
					Image image = createImage(imageEntry, stream);
					reconcile.put(imageKey, image);
				} else {
					LOG.trace("Skipping image creation for '{}', because this image is not referenced.", imageName);
				}
			} else if (existing instanceof Image) {
				throw new IllegalArgumentException("Duplicate image file in ZIP: " + imageName);
			} else if (existing instanceof Belonging) {
				Belonging belonging = (Belonging)existing;
				LOG.trace("Image '{}' matched with {} #{} '{}', creating and associating them.",
						imageName, belonging.type, belonging.id, belonging.name);
				Image image = createImage(imageEntry, stream);
				setImage(belonging, image);
				reconcile.remove(imageKey);
			} else {
				throw new IllegalStateException("Unknown object in image reconciler: " + existing);
			}
			if (!seenXML) {
				progress.imagesTotal++;
			}
		}

		public Image createImage(ZipEntry imageEntry, InputStream stream) throws IOException {
			long time = imageEntry.getTime();
			Long dbTime = time != -1? time : null;
			byte[] imageContents = IOTools.readBytes(stream, Math.max(0, imageEntry.getSize()));
			long imageId = db.addImage(imageContents, dbTime);
			return new Image(imageEntry, imageId);
		}

		public void setImage(Belonging belonging, Image image) {
			switch (belonging.type) {
				case Property:
					db.setPropertyImage(belonging.id, image.imageId);
					break;
				case Room:
					db.setRoomImage(belonging.id, image.imageId);
					break;
				case Item:
					db.setItemImage(belonging.id, image.imageId);
					break;
				default:
					throw new IllegalArgumentException(belonging.type + " cannot have images.");
			}
			progress.imagesDone++;
		}

		public void foundImageReference(Type type, long id, String name, String imageName) {
			Belonging belonging = new Belonging(type, id, name, imageName);
			@SuppressWarnings("UnnecessaryLocalVariable")
			String imageKey = imageName;
			Object existing = reconcile.get(imageKey);
			if (existing == null) {
				LOG.trace("Saving {} #{} '{}', in hopes of finding '{}' later.",
						belonging.type, belonging.id, belonging.name, imageName);
				reconcile.put(imageKey, belonging);
			} else if (existing instanceof Belonging) {
				throw new IllegalArgumentException("Duplicate image reference in XML: "
						+ name + " and " + ((Belonging)existing).name + " both reference " + imageName);
			} else if (existing instanceof Image) {
				Image image = (Image)existing;
				LOG.trace("{} #{} '{}' matched '{}', associating them.",
						belonging.type, belonging.id, belonging.name, imageName);
				setImage(belonging, image);
				reconcile.remove(imageKey);
			} else {
				throw new IllegalStateException("Unknown object in image reconciler: " + existing);
			}
		}

		public void cleanup() {
			for (Object existing : reconcile.values()) {
				if (existing instanceof Image) {
					// found image, but it wasn't referenced by any belongings
					// if it was, it would've been removed already from the collection
					Image image = (Image)existing;
					LOG.trace("Deleting image '{}' from database, it wasn't referenced.", image.entry);
					db.deleteImage(image.imageId);
				} else if (existing instanceof Belonging) {
					// found belonging, but its reference wasn't resolved by an image in the zip
					// if it was, it would've been removed already from the collection
					Belonging belonging = (Belonging)existing;
					warning(res.getString(R.string.backup_import_invalid_image, belonging.name, belonging.image));
				}
			}
		}
	}
}
