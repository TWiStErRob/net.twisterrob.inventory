package net.twisterrob.inventory.build.database.generator

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertTrue
import java.io.File
import java.io.StringWriter
import java.io.Writer

fun renderAsCategories(@Language("xml") xml: String, svgFolder: File? = null): List<Category> {
	val writer = StringWriter()
	val printer = CapturingPrinter()
	DatabaseGenerator(printer, svgFolder).transform(xml.reader(), writer)
	assertThat(writer.toString(), equalTo(""))
	assertTrue(printer.startCalled)
	assertTrue(printer.endCalled)
	return printer.prints
}

fun renderAsText(printer: Printer, @Language("xml") xml: String, svgFolder: File? = null): String {
	val writer = StringWriter()
	DatabaseGenerator(printer, svgFolder).transform(xml.reader(), writer)
	return writer.toString()
}

private class CapturingPrinter : Printer {

	var startCalled: Boolean = false
	var endCalled: Boolean = false
	val prints: MutableList<Category> = mutableListOf()

	override fun start(output: Writer) {
		startCalled = true
	}

	override fun print(c: Category, output: Writer) {
		prints.add(c)
	}

	override fun finish(output: Writer) {
		endCalled = true
	}
}
