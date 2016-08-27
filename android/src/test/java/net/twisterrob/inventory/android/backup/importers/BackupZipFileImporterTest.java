package net.twisterrob.inventory.android.backup.importers;

import java.io.*;

import org.mockito.InjectMocks;

import net.twisterrob.android.utils.tools.IOTools;

public class BackupZipFileImporterTest extends BackupZipImporterTestBase {
	@InjectMocks BackupZipFileImporter realImporter;

	@Override protected void callImport(InputStream stream) throws Exception {
		File file = temp.newFile();
		IOTools.copyStream(stream, new FileOutputStream(file));
		realImporter.importFrom(file);
	}
}
