package net.twisterrob.inventory.build.database.generator

@Suppress("detekt.DataClassShouldBeImmutable") // TODO review what's wrong with the model.
internal data class Category(
	var parent: Category? = null,
	var name: String? = null,
	var id: Int = INVALID_ID,
	var level: Int = 0,
	var icon: String? = null,
) {

	companion object {

		const val INVALID_ID: Int = Int.MIN_VALUE

		val INDIVIDUAL_ID_RANGE = -1 until 1000
	}
}
