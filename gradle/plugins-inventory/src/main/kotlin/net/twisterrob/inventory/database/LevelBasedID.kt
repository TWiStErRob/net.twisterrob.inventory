package net.twisterrob.inventory.database

import kotlin.math.pow

internal class LevelBasedID {

	private val levels = IntArray(MAX_LEVEL)

	private var lastLevel = 0

	private fun composeID(): Int {
		var id = 0
		for (level in levels.indices) {
			id += 10.0.pow(MAX_LEVEL - level - 1).toInt() * levels[level]
		}
		return id
	}

	fun newItem(level: Int): Int {
		if (level < 0 || MAX_LEVEL <= level) {
			throw IndexOutOfBoundsException("Invalid level: $level, must be between 0 and ${MAX_LEVEL - 1}")
		}
		check(!(0 < level && levels[level] == MAX_PER_LEVEL - 1)) {
			"Level $level cannot have more than $MAX_PER_LEVEL items."
		}
		check(lastLevel + 1 >= level) {
			"Cannot go deeper with skipping intermediate levels. Last: $lastLevel current: $level"
		}
		if (level < lastLevel) {
			var lvl = lastLevel
			while (level < lvl) {
				levels[lvl] = 0
				--lvl
			}
		}
		lastLevel = level
		levels[level]++
		return composeID()
	}

	companion object {

		internal const val MAX_PER_LEVEL: Int = 10
		internal const val MAX_LEVEL = 4
	}
}
