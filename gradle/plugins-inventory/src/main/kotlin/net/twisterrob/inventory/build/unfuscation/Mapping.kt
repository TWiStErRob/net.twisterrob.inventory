package net.twisterrob.inventory.build.unfuscation

import java.io.File
import java.io.Writer

internal class Mapping {

	fun remap(source: File, target: File) {
		target.writer().use { writer ->
			source.readLines().forEachIndexed { num, line ->
				processLine(writer, line, num)
			}
		}
	}

	// CONSIDER supporting -applymapping by using the names from the original -applymapping file
	private fun processLine(out: Writer, line: String, num: Int) {
		val match = MAPPING_PATTERN.matchEntire(line)?.groups
		requireNotNull(match) { "Line #${num} is not recognized: ${line}" }
		try {
			val originalName = match["name"]!!.value
			val obfuscatedName = match["obfuscated"]!!.value
			val newName =
				if (originalName == obfuscatedName)
					obfuscatedName
				else
					unfuscate(originalName, obfuscatedName)
			out.write(line.substring(0, match[MAPPING_PATTERN_OBFUSCATED_INDEX]!!.range.first))
			out.write(newName)
			out.write(line.substring(match[MAPPING_PATTERN_OBFUSCATED_INDEX]!!.range.last + 1))
			out.write("\n")
		} catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
			val message = buildString {
				append("Line #${num} failed: ${line}\n")
				for (group in 0..match.size) {
					append("Group #${group}: '${match[group]!!.value}'\n")
				}
			}
			throw IllegalArgumentException(message, ex)
		}
	}

	private fun unfuscate(original: String, obfuscated: String): String {
		// Reassemble the names with something readable, but still breaking changes.
		val origName = getName(original)
		val obfName = getName(obfuscated).takeIf { it != origName } ?: ""
		val packName = getPackage(original)
		return if (packName.isEmpty())
			"${origName}_${obfName}"
		else
			"${packName}.${origName}_${obfName}"
	}

	private fun getPackage(name: String): String =
		name.substringBeforeLast('.', missingDelimiterValue = "")

	private fun getName(name: String): String =
		name.substring(name.lastIndexOf('.') + 1)

	companion object {

		@Suppress(
			"RegExpUnexpectedAnchor", // False positive: .trimIndent() removes spaces.
			"RegExpRepeatedSpace", // Match actual line contents, rather than more complex regex.
			"RegExpUnnecessaryNonCapturingGroup", // For consistency.
		)
		private val MAPPING_PATTERN: Regex = Regex(
			"""
				^(?<member>    )?(?<location>\d+:\d+:)?(?:(?<type>.*?) )?(?<name>.*?)(?:\((?<args>.*?)\))?(?: -> )(?<obfuscated>.*?)(?<class>:?)$
		    """.trimIndent()
		)
		private const val MAPPING_PATTERN_OBFUSCATED_INDEX: Int = 6
	}
}
