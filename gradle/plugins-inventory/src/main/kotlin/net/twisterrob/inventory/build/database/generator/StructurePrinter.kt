package net.twisterrob.inventory.build.database.generator

import java.io.IOException
import java.io.Writer

internal class StructurePrinter : Printer {

	override fun start(output: Writer) {
		// no op
	}

	@Throws(IOException::class)
	override fun print(c: Category, output: Writer) {
		val indent = "\t".repeat(c.level + 1)

		val idString =
			if (c.id == Category.INVALID_ID)
				"INVALID"
			else
				c.id.toString().padStart(LevelBasedID.MAX_LEVEL + 1, ' ')

		@Suppress("MagicNumber")
		val mark =
			if (c.id % 10 >= 8 || c.id % 100 / 10 >= 8 || c.id % 1000 / 100 >= 8) "*" else " "

		output.write("${c.level}${mark} / ${idString}${indent}${c.name}, icon='${c.icon}'\n")
	}

	override fun finish(output: Writer) {
		// no op
	}
}
