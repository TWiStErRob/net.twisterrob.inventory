package net.twisterrob.inventory.android.content.contract;

public interface ParentColumns {
	String ID = CommonColumns.ID;
	String NAME = CommonColumns.NAME;
	String IMAGE = CommonColumns.IMAGE;
	String TYPE_IMAGE = CommonColumns.TYPE_IMAGE;
	String TYPE = "parentType";

	enum Type {
		Category("category"),
		Property("property"),
		Room("room"),
		Root("root"),
		Item("item");

		private final String string;

		private Type(String string) {
			this.string = string;
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
