package net.twisterrob.inventory.build.database.generator

import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.PrintWriter
import java.io.Reader
import java.io.Writer
import java.util.Stack
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader

internal class DatabaseGenerator(
	private val printer: Printer,
	private val svgFolder: File?
) {

	private val parents = Stack<Category>()
	private val level = LevelBasedID()

	@Throws(XMLStreamException::class, IOException::class)
	fun transform(input: Reader, output: Writer) {
		val xmlInputFactory = XMLInputFactory.newInstance()
		val xml = xmlInputFactory.createXMLStreamReader(input)

		printer.start(output)
		while (xml.hasNext()) {
			when (xml.next()) {
				XMLStreamConstants.START_ELEMENT -> {
					val category = parseCategory(xml) ?: continue
					handleCategory(category)
					printer.print(category, output)
				}
			}
		}
		printer.finish(output)
		output.flush()
	}

	private fun handleCategory(category: Category) {
		category.checkIcon(svgFolder)
		category.id = when (category.id) {
			Category.INVALID_ID ->
				try {
					level.newItem(category.level)
				} catch (ex: IllegalArgumentException) {
					throw IllegalStateException("Cannot create category ID for ${category}", ex)
				}
			in Category.INDIVIDUAL_ID_RANGE -> category.id
			else -> error("ID for ${category} cannot be outside ${Category.INDIVIDUAL_ID_RANGE}.")
		}
		// REPORT false positive during auto-konversion, because it messed up the precedence.
		while (parents.isNotEmpty() && category.level <= parents.peek().level) {
			parents.pop()
		}
		category.parent = if (parents.isEmpty()) null else parents.peek()
		// Try to set a new icon if doesn't have one yet.
		category.icon = category.icon ?: category.parent?.icon
		if (category.isNested()) {
			parents.push(category)
		}
	}

	@Suppress("ReturnCount")
	private fun parseCategory(xml: XMLStreamReader): Category? {
		if ("string" != xml.localName) return null

		val name = xml.getAttributeValue(NS, "name")
			?: error("Name is mandatory on a <string>")
		if (name.endsWith("_keywords") || name.endsWith("_description")) {
			return null
		}

		return Category().apply {
			this.name = name

			this.level = xml.getAttributeValue(NS, "level")
				?.toInt()
				?: error("level is mandatory for a category: ${name}")

			this.id = xml.getAttributeValue(NS, "id")
				?.toInt()
				?: Category.INVALID_ID

			this.icon = xml.getAttributeValue(NS, "icon")
				?.removePrefix("@raw/")
		}
	}

	companion object {

		private val NS: String? = null

		@JvmStatic
		@Throws(Throwable::class)
		fun main(args: Array<String>) {
			val svgFolder = File("..\\..\\data\\src\\main\\res\\raw")
			val input =
				FileReader(File("..\\..\\data\\src\\main\\res\\values\\strings_Categories.xml"))
			val output = PrintWriter(System.out, true)
			DatabaseGenerator(SQLPrinter(), svgFolder).transform(input, output)
		}
	}
}

private fun Category.checkIcon(svgFolder: File?) {
	if (svgFolder == null || icon == null) {
		return
	}
	require(svgFolder.resolve("${icon}.svg").exists()) {
		"Missing icon for ${this}."
	}
}

private fun Category.isNested(): Boolean =
	parent == null || parent!!.level <= this.level
