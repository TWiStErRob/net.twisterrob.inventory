import net.twisterrob.inventory.database.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*

class InventoryDatabaseTask extends DefaultTask {
	@InputFile File input
	@OutputFile File output
	@Optional @Input String conversion
	@Optional @Input File iconFolder

	@SuppressWarnings("GroovyUnusedDeclaration")
	@TaskAction
	void generate() {
		input.withReader { reader ->
			output.parentFile.mkdirs()
			output.withWriter { writer ->
				def printer = getPrinter()
				new DatabaseGenerator(printer, iconFolder).transform(reader, writer);
			}
		}
	}

	Printer getPrinter() {
		switch (conversion) {
			case null:
			case 'SQL':
				return new SQLPrinter()
			case 'structure':
				return new StructurePrinter()
			default:
				return (Printer)Class.forName(conversion).newInstance()
		}
	}
}
