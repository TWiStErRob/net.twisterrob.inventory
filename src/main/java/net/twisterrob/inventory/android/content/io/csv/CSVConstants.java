package net.twisterrob.inventory.android.content.io.csv;

import org.apache.commons.csv.CSVFormat;

public interface CSVConstants {
	int VERSION = 1;
	String ENCODING = "utf-8";
	CSVFormat FORMAT = CSVFormat.RFC4180
			.withHeader("type", "property", "room", "item", "image", "parent", "id", "version=" + VERSION)
			.withSkipHeaderRecord(true)
			.withIgnoreEmptyLines(true)
			.withIgnoreSurroundingSpaces(true)
			.withNullString("");
}
