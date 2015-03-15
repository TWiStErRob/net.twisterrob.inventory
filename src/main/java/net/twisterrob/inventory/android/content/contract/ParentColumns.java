package net.twisterrob.inventory.android.content.contract;

public interface ParentColumns extends CommonColumns {
	String PARENT_TYPE = "parentType";

	enum Type {
		Category("category", false),
		Property("property", true),
		Room("room", true),
		Root("root", false),
		Item("item", true);

		private final String string;
		private final boolean isMain;

		Type(String string, boolean isMain) {
			this.string = string;
			this.isMain = isMain;
		}

		public boolean isMain() {
			return isMain;
		}

		public static Type from(String string) {
			for (Type type : values()) {
				if (type.string.equals(string)) {
					return type;
				}
			}
			throw new IllegalArgumentException("Cannot find type for " + string);
		}
	}
}
