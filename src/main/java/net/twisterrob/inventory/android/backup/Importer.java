package net.twisterrob.inventory.android.backup;

import java.io.*;

import android.support.annotation.StringRes;

import net.twisterrob.inventory.android.content.contract.Type;

public interface Importer {
	void doImport(InputStream stream) throws Exception;

	interface ImportProgressHandler {
		void publishStart(long size);
		void publishIncrement();
		void warning(@StringRes int stringID, Object... args);
		void error(String message);
		void importImage(Type type, long id, String name, String image) throws IOException;
	}
}
