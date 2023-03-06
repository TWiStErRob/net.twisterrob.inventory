package net.twisterrob.inventory.build.database

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
 *         output = file(path to asset SQL file)
 *         conversion = "structure|SQL"
 *         iconFolder = file(path to SVG files)
 *     }
 * }
 * ```
 */
class InventoryDatabasePlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val entities = project.container<InventoryDatabaseEntity>()
		project.extensions.add("databaseEntities", entities)

		val allTasks = project.task("generateDataBase")
		allTasks.group = BasePlugin.BUILD_GROUP
		val allTasksClean = project.task("cleanGenerateDataBase")
		project.afterEvaluate {
			entities.all {
				val entity: InventoryDatabaseEntity = this
				//println "Creating task for ${entity.name} (${entity.input} --${entity.conversion}--> ${entity.output})"
				val genDBTaskName = "generateDataBase${entity.name.capitalized()}"
				val task = project.tasks.register<InventoryDatabaseTask>(genDBTaskName) {
					val task = this
					task.input = entity.input
					task.output = entity.output
					task.conversion = entity.conversion
					task.iconFolder = entity.iconFolder
				}
				allTasks.dependsOn(task)
				// clean task is automagically generated for every task that has output
				allTasksClean.dependsOn("clean${task.name.capitalized()}")
			}
		}
	}
}
