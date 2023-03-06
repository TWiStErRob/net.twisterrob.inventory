package net.twisterrob.inventory.build.database.generator

import java.io.IOException
import java.io.Writer

internal class SQLPrinter(
	private val debug: Boolean = false
) : Printer {

	private var prev: Category? = null

	@Throws(IOException::class)
	override fun start(output: Writer) {
		output.write("-- The following INSERT INTOs are generated via 'gradlew generateDB'\n\n")
	}

	@Throws(IOException::class)
	override fun print(c: Category, output: Writer) {
		var name = "'${c.name}',"
		val icon = "'${c.icon}'"
		val parent = if (c.parent == null) "NULL" else c.parent!!.id.toString()
		val id = c.id.toString()
		val debug = if (debug) " -- " + c.level else ""
		val union: String
		if (c.level == 0 && (c.id !in Category.INDIVIDUAL_ID_RANGE || prev == null)) {
			if (prev != null) {
				output.write(FOOTER)
			}
			output.write(HEADER)
			union = "     "
		} else {
			if (c.level == 1) {
				output.write("\n")
			} else if (1 < c.level) {
				val indent = "    ".repeat(c.level - 1)
				name = indent + name
			}
			union = "UNION"
		}
		output.write("\t${union} SELECT ${parent.padStart(5)}, ${id.padStart(5)}, ${name.padEnd(42)} ${icon}${debug}\n")
		prev = c
	}

	@Throws(IOException::class)
	override fun finish(output: Writer) {
		if (prev != null) {
			output.write(FOOTER)
		}
	}

	companion object {

		const val HEADER =
			"INSERT INTO Category\n\t           (parent,   _id, name,                                      image)\n"
		const val FOOTER = ";\n"
	}
}
