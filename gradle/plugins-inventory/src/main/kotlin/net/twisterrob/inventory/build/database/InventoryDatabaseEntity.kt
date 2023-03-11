package net.twisterrob.inventory.build.database

import org.gradle.api.Named
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

abstract class InventoryDatabaseEntity : Named {

	abstract val input: RegularFileProperty
	abstract val iconFolder: DirectoryProperty
	abstract val assetPath: Property<String>
	abstract val conversion: Property<String>
}
