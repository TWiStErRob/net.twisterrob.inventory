package net.twisterrob.inventory.build.database.generator

import kotlin.math.pow

internal class LevelBasedID {

	private val levels = IntArray(MAX_LEVEL)

	private var lastLevel: Int = 0

	fun newItem(level: Int): Int {
		require(level in 0 until MAX_LEVEL) {
			"Invalid level: $level, must be between 0 and ${MAX_LEVEL - 1}"
		}
		require(!(0 < level && levels[level] == MAX_PER_LEVEL - 1)) {
			"Level $level cannot have more than ${MAX_PER_LEVEL} items."
		}
		require(level <= lastLevel + 1) {
			"Cannot go deeper with skipping intermediate levels. Last: ${lastLevel} current: ${level}"
		}
		if (level < lastLevel) {
			levels.fill(0, level + 1, lastLevel + 1)
		}
		lastLevel = level
		levels[level]++
		return levels.composeID()
	}

	companion object {

		internal const val MAX_PER_LEVEL: Int = 10
		internal const val MAX_LEVEL = 4
	}
}

private fun IntArray.composeID(): Int =
	this
		.mapIndexed { level: Int, value: Int ->
			10.0.pow(LevelBasedID.MAX_LEVEL - level - 1).toInt() * value
		}
		.sum()
