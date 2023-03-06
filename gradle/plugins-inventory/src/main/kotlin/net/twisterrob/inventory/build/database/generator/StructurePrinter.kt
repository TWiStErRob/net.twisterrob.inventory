package net.twisterrob.inventory.build.database.generator

import java.io.IOException
import java.io.Writer
import java.util.Arrays
import java.util.Locale

class StructurePrinter : Printer {

	override fun start(output: Writer) {
		// no op
	}

	@Throws(IOException::class)
	override fun print(c: Category, output: Writer) {
		val indent = CharArray(c.level + 1)
		Arrays.fill(indent, '\t')
		val width = LevelBasedID.MAX_LEVEL + 1
		val idString = if (c.id == Category.INVALID_ID) "INVALID" else String.format(Locale.ROOT, "%" + width + "d", c.id)
		val mark = if (c.id % 10 >= 8 || c.id % 100 / 10 >= 8 || c.id % 1000 / 100 >= 8) "*" else " "
		output.write(String.format(Locale.ROOT, "%d%s / %s%s%s, icon='%s'\n",
			c.level, mark, idString, String(indent), c.name, c.icon))
	}

	override fun finish(output: Writer) {
		// no op
	}
}
