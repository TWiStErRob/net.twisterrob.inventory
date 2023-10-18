package net.twisterrob.inventory.android.database

import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DatabaseSqlIntegrationTests {

	@get:Rule val temp: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

	@Test(timeout = 5_000) fun testCreate() {
		val script = buildScript(createScripts)
		println(executeSQLite(temp.newFolder(), script))
	}

	@Test(timeout = 5_000) fun testUpgrade() {
		val script = buildScript(upgradeScripts)
		println(executeSQLite(temp.newFolder(), script))
	}

	@Test(timeout = 10_000) fun testCompareCreateAndUpgrade() {
		val create = buildScript(createScripts, ".output dump.sql\n.dump")
		val createFolder = temp.newFolder()
		println(executeSQLite(createFolder, create))
		val createStatements = parseSQLite(createFolder.resolve("dump.sql"))

		val upgrade = buildScript(upgradeScripts, ".output dump.sql\n.dump")
		val upgradeFolder = temp.newFolder()
		println(executeSQLite(upgradeFolder, upgrade))
		val upgradeStatements = parseSQLite(upgradeFolder.resolve("dump.sql"))

		assertEquals(
			stringifySQLiteStatements(createStatements).sorted(),
			stringifySQLiteStatements(upgradeStatements).sorted(),
		)
	}

	@Test(timeout = 5_000) fun testDevelopment() {
		val script = buildScript(
			scripts = listOf(
				// Initialize once
				*devScripts.toTypedArray(),
				// Tear-down and initialize again
				*devScripts.toTypedArray()
			),
			testScript = DatabaseSqlIntegrationTests::class.java
				.requireResourceAsStream("quick-test.sql")
				.readText()
		)
		println(executeSQLite(temp.newFolder(), script))
	}

	companion object {

		private val assetsMain: File =
			File(requireProperty("net.twisterrob.inventory.database.mainFolder"))

		private val assetsDebug: File =
			File(requireProperty("net.twisterrob.inventory.database.debugFolder"))

		private val devScripts: List<File> =
			listOf(
				assetsMain.resolve("MagicHomeInventory.clean.sql"),
				assetsMain.resolve("MagicHomeInventory.schema.sql"),
				assetsMain.resolve("MagicHomeInventory.data.sql"),
				assetsMain.resolve("MagicHomeInventory.data.Categories.sql"),
				assetsMain.resolve("MagicHomeInventory.init.sql"),
				assetsDebug.resolve("MagicHomeInventory.development.sql"),
				assetsDebug.resolve("MagicHomeInventory.verify.sql"),
				assetsDebug.resolve("MagicHomeInventory.test.sql"),
			)

		private val createScripts: List<File> =
			listOf(
				assetsMain.resolve("MagicHomeInventory.schema.sql"),
				assetsMain.resolve("MagicHomeInventory.data.sql"),
				assetsMain.resolve("MagicHomeInventory.data.Categories.sql"),
				assetsMain.resolve("MagicHomeInventory.init.sql"),
			)

		private val upgradeScripts: List<File> =
			listOf(
				assetsMain.resolve("MagicHomeInventory.upgrade.1.sql"),
				assetsMain.resolve("MagicHomeInventory.data.sql"),
				assetsMain.resolve("MagicHomeInventory.data.Categories.sql"),
				assetsMain.resolve("MagicHomeInventory.init.sql"),
				*generateSequence(2) { it + 1 }
					.map { assetsMain.resolve("MagicHomeInventory.upgrade.${it}.sql") }
					.takeWhile(File::exists)
					.toList()
					.toTypedArray()
			)
	}
}

@Language("SQLite")
private fun buildScript(
	scripts: List<File>,
	testScript: String = "",
): String {
	fun StringBuilder.appendLog(text: String) {
		val safeText = text.replace("'", "''")
		appendLine("select STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW'), '${safeText}';\n")
	}

	fun StringBuilder.appendFile(file: File) {
		appendLog(file.absolutePath)
		appendLine(file.readText())
	}

	return buildString {
		appendLine(
			"""
				.nullvalue NULL
				PRAGMA foreign_keys = ON;
				PRAGMA recursive_triggers = TRUE; -- This is not possible before 3.6.18
			""".trimIndent()
		)
		scripts.forEach { appendFile(it) }
		appendLine(".backup 'prepared.db'")
		appendLog("Test script")
		appendLine(testScript)
		appendLine(".backup 'final.db'")
	}
}
