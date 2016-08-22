package net.twisterrob.inventory.android.backup;

public interface ZipImporter<T> {
	void importFrom(T input) throws Exception;
}
