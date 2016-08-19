package net.twisterrob.inventory.android.backup;

import java.io.*;

import org.junit.Before;
import org.mockito.*;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

import android.content.res.Resources;

import net.twisterrob.android.test.GetStringVarargsAnswer;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.R;

public class BackupZipFileImporterTest extends BackupZipImporterTestBase {
	@Mock private Resources res;
	@InjectMocks BackupZipFileImporter realImporter;

	@Before public void setUp() {
		when(res.getString(anyInt(), Matchers.anyVararg())).thenAnswer(new GetStringVarargsAnswer(R.string.class));
	}

	@Override protected Progress callImport(InputStream stream) throws IOException {
		File file = temp.newFile();
		IOTools.copyStream(stream, new FileOutputStream(file));
		return realImporter.importFrom(file);
	}
}
