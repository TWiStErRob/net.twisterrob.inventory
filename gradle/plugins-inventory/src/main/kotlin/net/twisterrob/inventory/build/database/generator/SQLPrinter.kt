package net.twisterrob.inventory.build.database.generator

import java.io.IOException
import java.io.Writer
import java.util.Locale

/*
INSERT INTO Category
	           (parent,  _id, name,                                      image)
	      SELECT   NULL,    -1, 'category_internal', 'category_unknown'
	UNION SELECT   NULL,     0, 'category_uncategorized',                  'category_unknown'
;
*/

class SQLPrinter : Printer {

	private var prev: Category? = null

	@Throws(IOException::class)
	override fun start(output: Writer) {
		output.write("-- The following INSERT INTOs are generated via 'gradlew generateDB'\n\n")
	}

	@Throws(IOException::class)
	override fun print(c: Category, output: Writer) {
		var name = "'" + c.name + "',"
		val icon = "'" + c.icon + "'"
		val parent = if (c.parent == null) "NULL" else c.parent!!.id.toString()
		val id = c.id.toString()
		val debug = "" // " -- " + c.level;
		val union: String
		if (c.level == 0 && (1000 <= c.id || prev == null)) {
			if (prev != null) {
				output.write(FOOTER)
			}
			output.write(HEADER)
			union = "     "
		} else {
			if (c.level == 1) {
				output.write("\n")
			} else if (1 < c.level) {
				name = String(CharArray(c.level - 1)).replace("\u0000", "    ") + name
			}
			union = "UNION"
		}
		output.write(String.format(Locale.ROOT, "\t%s ${SELECT}%s\n", union, parent, id, name, icon, debug))
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
		private const val SELECT = "SELECT %5s, %5s, %-42s %s"
	}
}
