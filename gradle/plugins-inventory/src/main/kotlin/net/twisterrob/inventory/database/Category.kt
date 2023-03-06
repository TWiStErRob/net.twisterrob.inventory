package net.twisterrob.inventory.database

class Category {

	var parent: Category? = null
		set(value) {
			field = value
			if (icon == null && value != null) {
				icon = value.icon
			}
		}

	var name: String? = null
	var id = INVALID_ID
	var level = 0
	var icon: String? = null

	override fun toString(): String {
		return "Category{" +
			"name='" + name + '\'' +
			", id=" + id +
			", level=" + level +
			", icon='" + icon + '\'' +
			'}'
	}


	companion object {

		const val INVALID_ID = Int.MIN_VALUE
	}
}
