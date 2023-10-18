package net.twisterrob.inventory.android.database

import net.twisterrob.inventory.database.sqlite.SQLiteParser
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor
import org.antlr.v4.runtime.tree.TerminalNode

fun stringifySQLiteStatements(parsed: SQLiteParser.ParseContext): List<String> =
	parsed
		.sql_stmt_list()
		.single()
		.sql_stmt()
		.map {
			it.accept(object : AbstractParseTreeVisitor<String>() {
				override fun defaultResult(): String =
					""

				override fun aggregateResult(aggregate: String, nextResult: String): String =
					"${aggregate} ${nextResult}"

				override fun visitTerminal(node: TerminalNode): String =
					when (node.symbol.type) {
						SQLiteParser.SCOL -> ";\n"
						else -> node.symbol.text
					}
			})
		}
