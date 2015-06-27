import org.gradle.api.*
/**
 * Use it as
 * <code>
 *     apply plugin: InventoryDatabasePlugin
 *     databaseEntities {
 *         categories {
 *             input = file(path to Android res xml with Strings)
 *             output = file(path to asset SQL file)
 *             conversion = "structure|SQL"
 *         }
 *     }
 * </code>
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class InventoryDatabasePlugin implements Plugin<Project> {
	void apply(Project project) {
		def entities = project.container(InventoryDatabaseEntity)
		project.extensions.databaseEntities = entities

		def allTasks = project.task('generateDataBase')
		def allTasksClean = project.task('cleanGenerateDataBase')
		project.afterEvaluate {
			entities.all { entity ->
				//println "Creating task for ${entity.name} (${entity.input} --${entity.conversion}--> ${entity.output})"
				def task = project.task(type: InventoryDatabaseTask, "generateDataBase${entity.name.capitalize()}") {
					input = entity.input
					output = entity.output
					conversion = entity.conversion
				}
				allTasks.dependsOn task
				// clean task is automagically generated for every task that has output
				allTasksClean.dependsOn "clean${task.name.capitalize()}"
			}
		}
	}
}

class InventoryDatabaseEntity {
	def input
	def output
	String conversion

	final String name
	InventoryDatabaseEntity(String name) {
		this.name = name
	}
}
