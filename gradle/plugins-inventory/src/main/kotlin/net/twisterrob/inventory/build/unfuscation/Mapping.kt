package net.twisterrob.inventory.build.unfuscation

import java.io.File
import java.io.Writer
import java.util.regex.Pattern

@SuppressWarnings("UnnecessaryQualifiedReference")
internal class Mapping(
	private val source: File
) {
	companion object {
		private val MAPPING_PATTERN: Pattern = Pattern.compile("""
			^(?<member>    )?(?<location>\d+:\d+:)?(?:(?<type>.*?) )?(?<name>.*?)(?:\((?<args>.*?)\))?(?: -> )(?<obfuscated>.*?)(?<class>:?)$
	    """.trimIndent())
		private val MAPPING_PATTERN_OBFUSCATED_INDEX: Int = 6
	}


	fun remap(target: File) {
		target.writer().use { writer ->
			source.readLines().forEachIndexed { num, line -> processLine(writer, line, num) }
		}
	}

	// CONSIDER supporting -applymapping by using the names from the original -applymapping file
	private fun processLine(out: Writer, line: String, num: Int) {
		val m = MAPPING_PATTERN.matcher(line)
		if (!m.find()) {
			throw IllegalArgumentException("Line #${num} is not recognized: ${line}")
		}
		try {
			val originalName = m.group("name")
			val obfuscatedName = m.group("obfuscated")
			val newName = if (originalName.equals(obfuscatedName)) obfuscatedName else unfuscate(originalName, obfuscatedName)
			out.write(line.substring(0, m.start(MAPPING_PATTERN_OBFUSCATED_INDEX)))
			out.write(newName)
			out.write(line.substring(m.end(MAPPING_PATTERN_OBFUSCATED_INDEX)))
			out.write("\n")
		} catch (ex: Exception) {
			val sb = StringBuilder("Line #${num} failed: ${line}\n")
			0.rangeTo(m.groupCount()).forEach { sb.append("Group #${it}: '${m.group(it)}'\n") }
			throw IllegalArgumentException(sb.toString(), ex)
		}
	}

	private fun unfuscate(original: String, obfuscated: String): String {
		// reassemble the names with something readable, but still breaking changes
		val origName = getName(original)
		var obfName = getName(obfuscated)
		obfName = if (obfName.equals(origName)) "" else obfName
		return getPackage(original) + origName + '_' + obfName
	}
	private fun getPackage(name: String): String {
		val lastDot = name.lastIndexOf('.')
		return if (lastDot < 0) "" else name.substring(0, lastDot + 1)
	}
	private fun getName(name: String): String {
		return name.substring(name.lastIndexOf('.') + 1)
	}
}
