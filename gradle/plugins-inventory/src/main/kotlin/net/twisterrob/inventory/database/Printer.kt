package net.twisterrob.inventory.database

import java.io.IOException
import java.io.Writer

interface Printer {

	@Throws(IOException::class)
	fun start(output: Writer)

	@Throws(IOException::class)
	fun print(c: Category, output: Writer)

	@Throws(IOException::class)
	fun finish(output: Writer)
}
