package net.twisterrob.inventory.android.content.io;

import org.apache.commons.csv.CSVFormat;

import net.twisterrob.inventory.android.content.contract.*;

public interface CSVConstants {
	int VERSION = 1;
	String[] HEADERS = {
			"type",
			"property",
			"room",
			"item",
			"image",
			"parent",
			"id",
			"version=" + VERSION
	};
	String[] COLUMNS = {
			"typeName",
			Item.PROPERTY_NAME,
			Item.ROOM_NAME,
			"itemName",
			CommonColumns.IMAGE,
			"parent",
			CommonColumns.ID,
			null
	};

	String ENCODING = "utf-8";
	CSVFormat FORMAT = CSVFormat.RFC4180
			.withHeader(HEADERS)
			.withSkipHeaderRecord(true)
			.withIgnoreEmptyLines(true)
			.withIgnoreSurroundingSpaces(true)
			.withNullString("");
}
