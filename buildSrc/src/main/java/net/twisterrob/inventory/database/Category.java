package net.twisterrob.inventory.database;

class Category {
	static final int INVALID_ID = Integer.MIN_VALUE;
	Category parent;
	String name;
	int id = INVALID_ID;
	int level = 0;
	String icon;

	@Override public String toString() {
		return "Category{" +
				"name='" + name + '\'' +
				", id=" + id +
				", level=" + level +
				", icon='" + icon + '\'' +
				'}';
	}
	public void setParent(Category parent) {
		this.parent = parent;
		if (icon == null && parent != null) {
			icon = parent.icon;
		}
	}
}
