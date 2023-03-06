package net.twisterrob.inventory.database

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

class DatabaseGenerator(
	private val printer: Printer,
	private val svgFolder: File?
) {

	private val parents = Stack<Category>()
	private val level = LevelBasedID()

	@Throws(XMLStreamException::class, IOException::class)
	fun transform(input: Reader?, output: Writer) {
		val xmlInputFactory = XMLInputFactory.newInstance()
		val xml = xmlInputFactory.createXMLStreamReader(input)
		printer.start(output)
		while (xml.hasNext()) {
			val type = xml.next()
			if (type == XMLStreamConstants.START_ELEMENT && "string" == xml.localName) {
				val category = parseCategory(xml)
				if (category != null) {
					//System.out.println(); // new category found
					//printAttributes(xml);

					checkCategory(category)
					if (category.id == Category.INVALID_ID) {
						try {
							category.id = level.newItem(category.level)
						} catch (ex: Exception) {
							throw IllegalStateException("Cannot create category ID for $category", ex)
						}
					} else if (1000 <= category.id) {
						throw IllegalStateException("Specific ID cannot be bigger than 1000")
					}
					var parent: Category?
					@Suppress("UNINITIALIZED_VARIABLE") // REPORT false positive
					while ((if (parents.empty()) null else parents.peek().also { parent = it }) != null
						&& category.level <= parent!!.level) {
						parents.pop()
					}
					category.parent = parent
					printer.print(category, output)
					if (parent == null || parent!!.level <= category.level) {
						parents.push(category)
					}
				}
			}
		}
		printer.finish(output)
		output.flush()
	}

	private fun parseCategory(xml: XMLStreamReader): Category? {
		val name = xml.getAttributeValue(NS, "name")
			?: throw IllegalArgumentException("Name is mandatory on a <string>")
		if (name.endsWith("_keywords") || name.endsWith("_description")) {
			return null
		}

		val c = Category()
		c.name = name

		val level = xml.getAttributeValue(NS, "level")
		if (level != null) {
			c.level = level.toInt()
		} else {
			throw IllegalArgumentException("level is mandatory for a category: $name")
		}

		val id = xml.getAttributeValue(NS, "id")
		if (id != null) {
			c.id = id.toInt()
		}

		c.icon = xml.getAttributeValue(NS, "icon")
		if (c.icon != null && c.icon!!.startsWith("@raw/")) {
			c.icon = c.icon!!.substring("@raw/".length)
		}
		return c
	}

	private fun checkCategory(category: Category?) {
		if (svgFolder == null || category == null || category.icon == null) {
			return
		}
		require(File(svgFolder, category.icon + ".svg").exists()) {
			"Missing icon: $category"
		}
	}

	companion object {

		private val NS: String? = null
		
		@JvmStatic
		@Throws(Throwable::class)
		fun main(args: Array<String>) {
			val svgFolder = File("..\\..\\data\\src\\main\\res\\raw")
			val input = FileReader(File("..\\..\\data\\src\\main\\res\\values\\strings_Categories.xml"))
			val output = PrintWriter(System.out, true)
			DatabaseGenerator(SQLPrinter(), svgFolder).transform(input, output)
		}
	}
}
