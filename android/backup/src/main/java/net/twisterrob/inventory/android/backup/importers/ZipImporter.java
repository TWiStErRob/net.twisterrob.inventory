package net.twisterrob.inventory.android.backup.importers;

public interface ZipImporter<T> {
	void importFrom(T input) throws Exception;
}
