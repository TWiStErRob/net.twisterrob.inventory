package net.twisterrob.inventory.build.database

import net.twisterrob.inventory.build.database.generator.DatabaseGenerator
import net.twisterrob.inventory.build.database.generator.Printer
import net.twisterrob.inventory.build.database.generator.SQLPrinter
import net.twisterrob.inventory.build.database.generator.StructurePrinter
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class InventoryDatabaseTask : DefaultTask() {

	@get:InputFile
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val input: RegularFileProperty

	@get:Optional
	@get:InputDirectory
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val iconFolder: DirectoryProperty

	@get:Input
	abstract val assetPath: Property<String>

	@get:Optional
	@get:Input
	abstract val conversion: Property<String>

	@get:OutputDirectory
	abstract val output: DirectoryProperty

	@TaskAction
	fun generate() {
		val input = input.get().asFile
		input.reader().use { reader ->
			val outputDir = output.get().asFile.also { it.mkdirs() }
			val output = outputDir.resolve(assetPath.get())
			output.writer().use { writer ->
				val printer = getPrinter(conversion.orNull)
				DatabaseGenerator(printer, iconFolder.orNull?.asFile).transform(reader, writer)
			}
		}
	}

	private fun getPrinter(conversion: String?): Printer =
		when (conversion) {
			null -> SQLPrinter()
			"SQL" -> SQLPrinter()
			"structure" -> StructurePrinter()
			else -> Class.forName(conversion).getDeclaredConstructor().newInstance() as Printer
		}
}
