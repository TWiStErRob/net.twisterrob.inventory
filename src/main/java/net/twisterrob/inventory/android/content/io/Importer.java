package net.twisterrob.inventory.android.content.io;

import java.io.InputStream;

public interface Importer {
	void doImport(InputStream stream) throws Exception;
}
