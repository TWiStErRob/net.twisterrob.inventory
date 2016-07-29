package net.twisterrob.inventory.android.content.contract;

public abstract class ResourceNames {
	public static final String KEYWORDS = "_keywords";
	public static final String DESCRIPTION = "_description";

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
