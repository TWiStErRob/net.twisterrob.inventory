package net.twisterrob.inventory.android.database

import net.twisterrob.inventory.database.sqlite.SQLiteLexer
import net.twisterrob.inventory.database.sqlite.SQLiteParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

fun parseSQLite(file: File): SQLiteParser.ParseContext {
	println("Parsing file: ${file.toPath().toUri()}")
	val lexer = SQLiteLexer(CharStreams.fromStream(file.inputStream()))
	val tokenStream = CommonTokenStream(lexer)
	val parser = SQLiteParser(tokenStream)
	return parser.parse()
}
