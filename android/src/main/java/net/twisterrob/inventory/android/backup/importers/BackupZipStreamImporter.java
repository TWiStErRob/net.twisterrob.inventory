package net.twisterrob.inventory.android.backup.importers;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.*;

import org.slf4j.*;

import android.content.res.Resources;
import android.support.annotation.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.backup.ImportProgressHandler;
import net.twisterrob.inventory.android.backup.Importer.ImportImageGetter;
import net.twisterrob.inventory.android.backup.Progress.Phase;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.Type;
import net.twisterrob.java.io.NonClosableStream;
import net.twisterrob.java.utils.ObjectTools;

public class BackupZipStreamImporter implements ZipImporter<InputStream> {
	private static final Logger LOG = LoggerFactory.getLogger(BackupZipStreamImporter.class);

	private final @NonNull ImportProgressHandler progress;
	private final @NonNull Resources res;
	private final @NonNull XMLImporter importer;
	private final @NonNull ImportImageReconciler images;

	public BackupZipStreamImporter(Resources res, ImportProgressHandler progress) {
		this(res, new XMLImporter(res, App.db()), App.db(), progress);
	}
	@VisibleForTesting BackupZipStreamImporter(@NonNull Resources res,
			@NonNull XMLImporter importer, @NonNull Database db, @NonNull ImportProgressHandler progress) {
		this.res = ObjectTools.checkNotNull(res);
		this.progress = ObjectTools.checkNotNull(progress);
		this.importer = ObjectTools.checkNotNull(importer);
		// TODO extract param => tests don't need to check DB interactions in importer test
		this.images = new ImportImageReconciler(db, res, progress);
	}

	@Override public void importFrom(InputStream source) throws Exception {
		ZipInputStream zip = null;
		try {
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
					progress.progress.phase = Phase.Data;
					importer.doImport(nonClosableZip, progress, images);
					progress.progress.imagesTotal = progress.progress.imagesDone + images.size();
					//progress.publishProgress();
					images.hasData();
					LOG.trace("Finished importing XML data");
				} else if (image.matcher(entry.getName()).matches()) {
					LOG.trace("Importing image file: {}", entry.getName());
					progress.progress.phase = Phase.Images;
					long time = entry.getTime();
					Long dbTime = time != -1? time : null;
					images.foundImageFile(entry.getName(), nonClosableZip, dbTime);
					progress.publishProgress();
					LOG.trace("Finished importing image file");
				} else {
					LOG.debug("Skipping unknown file: {}", entry.getName());
				}
			}
			images.close();
			if (!seenEntry) {
				throw new IllegalArgumentException(String.format("The import is not a valid %s backup: %s",
						res.getString(R.string.app_name), "missing data file " + Paths.BACKUP_DATA_FILENAME));
			}
		} finally {
			IOTools.ignorantClose(zip);
		}
	}

	@VisibleForTesting static class ImportImageReconciler implements ImportImageGetter, Closeable {
		private static final Logger LOG = LoggerFactory.getLogger(ImportImageReconciler.class);

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
			final String name;

			private Image(String name, long imageId) throws IOException {
				this.name = name;
				this.imageId = imageId;
			}
		}

		private final Database db;
		private final Resources res;
		private final ImportProgressHandler progress;
		private final Map<String, Object> reconcile = new HashMap<>();
		private boolean seenXML;

		public ImportImageReconciler(Database db, Resources res, ImportProgressHandler progress) {
			this.db = ObjectTools.checkNotNull(db);
			this.res = ObjectTools.checkNotNull(res);
			this.progress = ObjectTools.checkNotNull(progress);
		}

		public int size() {
			return reconcile.size();
		}

		public void hasData() {
			seenXML = true;
		}

		public void foundImageFile(String imageName, InputStream stream, Long time) throws IOException {
			String imageKey = imageName;
			Object existing = reconcile.get(imageKey);
			if (existing == null) {
				if (!seenXML) {
					LOG.trace("Creating image '{}', in hopes of future reference.", imageName);
					Image image = createImage(imageName, time, stream);
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
				Image image = createImage(imageName, time, stream);
				setImage(belonging, image);
				reconcile.remove(imageKey);
			} else {
				throw unknown(existing);
			}
			if (!seenXML) {
				progress.imageTotalIncrement();
			}
		}

		private Image createImage(String name, Long time, InputStream stream) throws IOException {
			byte[] imageContents = IOTools.readBytes(stream);
			long imageId = db.addImage(imageContents, time);
			return new Image(name, imageId);
		}

		private void setImage(Belonging belonging, Image image) {
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
			progress.imageIncrement();
		}

		@Override public void importImage(Type type, long id, String name, String imageName) {
			if (seenXML) {
				throw new IllegalStateException("Cannot import belongings once data import is finished.");
			}
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
				throw unknown(existing);
			}
		}

		@Override public void close() {
			for (Object existing : reconcile.values()) {
				if (existing instanceof Image) {
					// found image, but it wasn't referenced by any belongings
					// if it was, it would've been removed already from the collection
					Image image = (Image)existing;
					LOG.trace("Deleting image '{}' from database, it wasn't referenced.", image.name);
					db.deleteImage(image.imageId);
				} else if (existing instanceof Belonging) {
					// found belonging, but its reference wasn't resolved by an image in the zip
					// if it was, it would've been removed already from the collection
					Belonging belonging = (Belonging)existing;
					progress.warning(
							res.getString(R.string.backup_import_invalid_image, belonging.name, belonging.image));
				}
			}
		}

		private RuntimeException unknown(Object existing) {
			return new IllegalStateException("Unknown object in image reconciler: " + existing);
		}
	}
}
