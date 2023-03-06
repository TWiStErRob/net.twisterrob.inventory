package net.twisterrob.inventory.build.database

import net.twisterrob.inventory.build.database.generator.DatabaseGenerator
import net.twisterrob.inventory.build.database.generator.Printer
import net.twisterrob.inventory.build.database.generator.SQLPrinter
import net.twisterrob.inventory.build.database.generator.StructurePrinter
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class InventoryDatabaseTask : DefaultTask() {

	@get:InputFile
	abstract val input: RegularFileProperty

	@get:OutputFile
	abstract val output: RegularFileProperty

	@get:Optional
	@get:Input
	abstract val conversion: Property<String>

	@get:Optional
	@get:InputDirectory
	abstract val iconFolder: DirectoryProperty

	@TaskAction
	fun generate() {
		val input = input.get().asFile
		input.reader().use { reader ->
			val output = output.get().asFile.also { it.parentFile.mkdirs() }
			output.writer().use { writer ->
				val printer = getPrinter(conversion.orNull)
				DatabaseGenerator(printer, iconFolder.orNull?.asFile).transform(reader, writer)
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
