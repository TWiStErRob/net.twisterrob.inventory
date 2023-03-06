package net.twisterrob.inventory.build.database

import net.twisterrob.inventory.build.database.generator.DatabaseGenerator
import net.twisterrob.inventory.build.database.generator.Printer
import net.twisterrob.inventory.build.database.generator.SQLPrinter
import net.twisterrob.inventory.build.database.generator.StructurePrinter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class InventoryDatabaseTask : DefaultTask() {

	@get:InputFile
	abstract var input: File

	@get:OutputFile
	abstract var output: File

	@get:Optional
	@get:Input
	abstract var conversion: String?

	@get:Optional
	@get:InputDirectory
	abstract var iconFolder: File?

	@TaskAction
	fun generate() {
		input.reader().use { reader ->
			output.parentFile.mkdirs()
			output.writer().use { writer ->
				val printer = getPrinter(conversion)
				DatabaseGenerator(printer, iconFolder).transform(reader, writer)
			}
		}
	}

	private fun getPrinter(conversion: String?): Printer =
		when (conversion) {
			null, "SQL" -> SQLPrinter()
			"structure" -> StructurePrinter()
			else -> Class.forName(conversion).getDeclaredConstructor().newInstance() as Printer
		}
}
