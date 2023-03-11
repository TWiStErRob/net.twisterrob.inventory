package net.twisterrob.inventory.build.database

import net.twisterrob.gradle.android.androidComponents
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.register

/**
 * Use it as
 * ```gradle
 * apply plugin: InventoryDatabasePlugin
 * databaseEntities {
 *     categories {
 *         input = file(path to Android res xml with Strings)
 *         iconFolder = file(path to SVG files)
 *         assetPath = path of the SQL file under Android Assets
 *         conversion = "structure|SQL"
 *     }
 * }
 * ```
 */
class InventoryDatabasePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val entities = project.container<InventoryDatabaseEntity>()
		project.extensions.add("databaseEntities", entities)

		val allTasks = project.tasks.register("generateDataBase") {
			group = BasePlugin.BUILD_GROUP
		}
		val allTasksClean = project.tasks.register("cleanGenerateDataBase")
		entities.configureEach {
			val entity: InventoryDatabaseEntity = this
			val taskName = "generateDataBase${entity.name.capitalized()}"
			val transformation = "(${entity.input} --${entity.conversion}--> ${entity.assetPath}"
			project.logger.debug("Creating task ${taskName} for ${entity.name}: ${transformation})")
			val task = project.tasks.register<InventoryDatabaseTask>(taskName) {
				this.input.convention(entity.input)
				this.assetPath.convention(entity.assetPath)
				this.conversion.convention(entity.conversion)
				this.iconFolder.convention(entity.iconFolder)
			}
			allTasks.configure { dependsOn(task) }
			// clean task is automagically generated for every task that has output
			allTasksClean.configure { dependsOn("clean${task.name.capitalized()}") }
			project.androidComponents.onVariants { variant ->
				@Suppress("UnstableApiUsage")
				variant.sources.assets!!
					.addGeneratedSourceDirectory(task, InventoryDatabaseTask::output)
			}
		}
	}
}
