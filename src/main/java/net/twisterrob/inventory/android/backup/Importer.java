package net.twisterrob.inventory.android.backup;

import java.io.*;

import android.support.annotation.*;

import net.twisterrob.inventory.android.content.contract.Type;

public interface Importer {
	void doImport(@NonNull InputStream stream, @Nullable ImportProgressHandler progress) throws Exception;

	// TODO split this interface into ImageGetter and ProgressHandler
	interface ImportProgressHandler {
		void publishStart(int size);
		void publishIncrement();
		void warning(String message);
		void error(String message);
		void importImage(Type type, long id, String name, String image) throws IOException;
	}

	interface ImportCallbacks {
		void importStarting();
		void importProgress(@NonNull Progress progress);
		void importFinished(@NonNull Progress progress);
	}
}
