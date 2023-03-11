package net.twisterrob.inventory.build.database

import net.twisterrob.gradle.android.androidComponents
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy
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

		val allTasks = project.tasks.register("generateDataBase") {
			group = BasePlugin.BUILD_GROUP
		}
		val allTasksClean = project.tasks.register("cleanGenerateDataBase")
		entities.configureEach {
			val entity: InventoryDatabaseEntity = this
			//println "Creating task for ${entity.name} (${entity.input} --${entity.conversion}--> ${entity.output})"
			val genDBTaskName = "generateDataBase${entity.name.capitalized()}"
			val task = project.tasks.register<InventoryDatabaseTask>(genDBTaskName) {
				this.input.convention(entity.input)
				this.output.convention(entity.output)
				this.conversion.convention(entity.conversion)
				this.iconFolder.convention(entity.iconFolder)
			}
			allTasks.configure { dependsOn(task) }
			// clean task is automagically generated for every task that has output
			allTasksClean.configure { dependsOn("clean${task.name.capitalized()}") }
			val copyTask = project.tasks.register<Copy>("copy") {
				from(task)
				into(project.layout.buildDirectory.dir("generated/assets_database_entities"))
			}
			project.androidComponents.apply {
				onVariants { variant ->
					variant.sources.assets?.addGeneratedSourceDirectory(copyTask) { task ->
						project
							.objects
							.directoryProperty()
							//.fileValue(task.destinationDir)
							.fileProvider(copyTask.map { task.destinationDir })
					}
				}
			}
		}
	}
}
