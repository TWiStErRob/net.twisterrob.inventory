package net.twisterrob.inventory.build.tests

import com.android.build.gradle.internal.testing.utp.UtpDependency
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Helper task to generate a Version Catalog TOML to retrieve UTP dependencies.
 * `build.gradle`:
 * ```gradle
 * plugins {
 *   id("java")
 * }
 * dependencies {
 *   compileOnly(utp.bundles.utp)
 * }
 * ```
 * `settings.gradle`:
 * ```gradle
 * dependencyResolutionManagement {
 *   versionCatalogs {
 *     create("utp") {
 *       from(files("gradle/utp.versions.toml"))
 *     }
 *   }
 * }
 * ```
 * After IDEA sync the artifacts and classes listed in `utp.versions.toml` will be available to browse.
 */
@CacheableTask
abstract class GenerateUtpDependenciesTask : DefaultTask() {

	init {
		utpClasspath.from(UtpDependency::class.java.protectionDomain.codeSource.location)
		output.convention { project.rootDir.resolve("gradle/utp.versions.toml") }
	}

	@get:InputFiles
	@get:Classpath
	abstract val utpClasspath: ConfigurableFileCollection

	@get:OutputFile
	abstract val output: RegularFileProperty

	@TaskAction
	fun run() {
		output.get().asFile.writeText(
			buildString {
				appendLine("[libraries]")
				UtpDependency.values().forEach { dep ->
					appendLine("# ${dep.name}: ${dep.mainClass}")
					appendLine("""${dep.artifactId} = "${dep.mavenCoordinate()}"""")
				}
				val deps = UtpDependency.values().joinToString(", ") { dep ->
					"\"${dep.artifactId}\""
				}
				appendLine()
				appendLine("[bundles]")
				appendLine("utp = [${deps}]")
			}
		)
	}
}
