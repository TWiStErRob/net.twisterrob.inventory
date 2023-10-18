package net.twisterrob.inventory.android.database

import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.io.File
import java.io.StringWriter
import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.concurrent.thread

fun executeSQLite(dir: File, @Language("SQLite") sql: String): String {
	val script = dir.resolve("sqlite.sql").also { it.writeText(sql) }
	println("Executing ${script}...")
	try {
		return executeSQLite(dir, script)
	} finally {
		println("Finished executing ${script}...")
	}
}

private fun executeSQLite(dir: File, script: File): String {
	val process = ProcessBuilder()
		.directory(dir)
		.command(
			sqliteExecutable().absolutePath,
			"-init",
			script.absolutePath,
			// Fail fast if anything errors.
			"-bail",
			// https://stackoverflow.com/a/76344213/253468
			"-unsafe-testing",
			// Force non-interactive mode.
			"-batch"
		)
		.redirectOutput(Redirect.PIPE)
		.redirectError(Redirect.PIPE)
		.start()
	// Need to consume streams, otherwise external process will block: https://stackoverflow.com/a/3285479/253468
	val out = StringWriter(); thread { process.inputStream.reader().copyTo(out) }
	val err = StringWriter(); thread { process.errorStream.reader().copyTo(err) }
	// Terminate SQLite's interactive REPL prompt, which is required even with -batch.
	process.outputStream.close()
	// Make sure it finishes, and in a reasonable amount of time.
	assertTrue("Timed out", @Suppress("Since15") process.waitFor(3, SECONDS))
	if (process.exitValue() != 0) {
		assertEquals(
			@Suppress("StringShouldBeRawString") // It would be more complex that way.
			"Non-zero exit value:\nstdout:\n${out.buffer}\nstderr:\n${err.buffer}",
			0,
			process.exitValue()
		)
	} else {
		assertEquals(
			"There should be no error output for normal execution.",
			"",
			err.buffer.toString()
		)
	}
	return out.buffer.toString()
}

private fun sqliteExecutable(): File {
	val androidSdk = File(requireProperty("net.twisterrob.inventory.database.androidSdk"))
	val variants = listOf("sqlite3.sh", "sqlite3", "sqlite3.bat", "sqlite3.exe")
	return variants
		.map { androidSdk.resolve("platform-tools/${it}") }
		.firstOrNull { it.exists() && it.isFile }
		?: error("Cannot find any of ${variants} in the Android SDK: ${androidSdk}")
}
