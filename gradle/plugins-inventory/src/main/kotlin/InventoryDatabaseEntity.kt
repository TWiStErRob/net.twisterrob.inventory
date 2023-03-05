import java.io.File

abstract class InventoryDatabaseEntity(
	val name: String
) {

	abstract var input: File
	abstract var output: File
	abstract var iconFolder: File
	abstract var conversion: String
}
