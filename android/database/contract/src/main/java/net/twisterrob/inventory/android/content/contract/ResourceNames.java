package net.twisterrob.inventory.android.content.contract;

public abstract class ResourceNames {
	private static final String KEYWORDS = "_keywords";
	private static final String DESCRIPTION = "_description";

	public static String getKeywordsName(String name) {
		return name + KEYWORDS;
	}

	public static String getDescriptionName(String name) {
		return name + DESCRIPTION;
	}

	private ResourceNames() {
		// prevent instantiation
	}
}
