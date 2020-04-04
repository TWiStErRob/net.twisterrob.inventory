import org.gradle.api.*
import org.gradle.api.plugins.BasePlugin

// TODEL @formatter https://youtrack.jetbrains.com/issue/IDEA-154077
//@formatter:off
/**
 * Use it as
 * <pre><code>
 *     apply plugin: InventoryDatabasePlugin
 *     databaseEntities {
 *         categories {
 *             input = file(path to Android res xml with Strings)
 *             output = file(path to asset SQL file)
 *             conversion = "structure|SQL"
 *             iconFolder = file(path to SVG files)
 *         }
 *     }
 * </code></pre>
 */
//@formatter:on
@SuppressWarnings("GroovyUnusedDeclaration")
class InventoryDatabasePlugin implements Plugin<Project> {
	void apply(Project project) {
		def entities = project.container(InventoryDatabaseEntity)
		project.extensions.add("databaseEntities", entities)

		def allTasks = project.task('generateDataBase')
		allTasks.group = BasePlugin.BUILD_GROUP
		def allTasksClean = project.task('cleanGenerateDataBase')
		project.afterEvaluate {
			entities.all { InventoryDatabaseEntity entity ->
				//println "Creating task for ${entity.name} (${entity.input} --${entity.conversion}--> ${entity.output})"
				def genDBTaskName = "generateDataBase${entity.name.capitalize()}"
				def task = project.tasks.register(genDBTaskName, InventoryDatabaseTask, {
					InventoryDatabaseTask task ->
						task.input = entity.input
						task.output = entity.output
						task.conversion = entity.conversion
						task.iconFolder = entity.iconFolder
				} as Action<InventoryDatabaseTask>)
				allTasks.dependsOn task
				// clean task is automagically generated for every task that has output
				allTasksClean.dependsOn "clean${task.name.capitalize()}"
			}
		}
	}
}

class InventoryDatabaseEntity {
	File input
	File output
	File iconFolder
	String conversion

	final String name
	InventoryDatabaseEntity(String name) {
		this.name = name
	}
}
