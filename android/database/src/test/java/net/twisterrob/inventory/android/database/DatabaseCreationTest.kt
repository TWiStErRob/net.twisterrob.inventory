package net.twisterrob.inventory.android.database

import net.twisterrob.inventory.database.sqlite.SQLiteLexer
import net.twisterrob.inventory.database.sqlite.SQLiteParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.TerminalNode
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class DatabaseCreationTest {
	@Test fun test() {
		val statements1 = parseContext(File("build\\database\\create\\dump.sql"))
		val statements2 = parseContext(File("build\\database\\upgrade\\dump.sql"))
		assertEquals(prepare(statements1).sorted(), prepare(statements2).sorted())
	}

	private fun prepare(parsed: SQLiteParser.ParseContext): List<String> =
		parsed
			.sql_stmt_list()
			.single()
			.sql_stmt()
			.map { it.accept(StringVisitor()) }

	private fun parseContext(file: File): SQLiteParser.ParseContext {
		//println("Parsing file: ${file.toPath().toUri()}")
		val lexer = SQLiteLexer(CharStreams.fromStream(file.inputStream()))
		val tokenStream = CommonTokenStream(lexer)
		val parser = SQLiteParser(tokenStream)
		return parser.parse()
	}
}

private class StringVisitor : AbstractParseTreeVisitor<String>() {
	override fun defaultResult(): String =
		""

	override fun aggregateResult(aggregate: String, nextResult: String): String =
		"${aggregate} ${nextResult}"

	override fun visitTerminal(node: TerminalNode): String =
		when (node.symbol.type) {
			SQLiteParser.SCOL -> ";\n"
			else -> node.symbol.text
		}
}
