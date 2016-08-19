package net.twisterrob.inventory.android.backup;

public interface ZipImporter<T> {
	/** @throws never */
	@SuppressWarnings({"JavaDoc", "JavadocReference"})
	Progress importFrom(T input);
}
