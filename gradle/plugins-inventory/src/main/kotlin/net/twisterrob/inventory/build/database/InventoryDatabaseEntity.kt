package net.twisterrob.inventory.build.database

import org.gradle.api.Named
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * DSL configuration for one database file generated from Android resources.
 */
abstract class InventoryDatabaseEntity : Named {

	/**
	 * XML resource file that supplies the category names.
	 */
	abstract val input: RegularFileProperty

	/**
	 * Directory containing the category SVG icons.
	 */
	abstract val iconFolder: DirectoryProperty

	/**
	 * Destination path of the generated SQL file relative to the Android assets folder.
	 */
	abstract val assetPath: Property<String>

	/**
	 * Output format used to generate the database file.
	 *
	 * Options: SQL, structure, null
	 */
	abstract val conversion: Property<String>
}
