package net.twisterrob.inventory.build.unfuscation

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.R8Task
import net.twisterrob.gradle.android.androidComponents
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.named
import java.io.File
import java.io.FileInputStream
import java.util.Locale

// Currently not used, this version was just a test to try to generate a mapping from *.jar/**/*.class files
@Suppress("UnstableApiUsage")
class GenerateDebugMappingPlugin : Plugin<Project> {

	@Suppress("LongMethod")
	override fun apply(project: Project) {
		(project.androidComponents as ApplicationAndroidComponentsExtension).onVariants { variant ->
			if (!variant.isMinifyEnabled) return@onVariants
			val mapping = variant.artifacts
				.get(SingleArtifact.OBFUSCATION_MAPPING_FILE)
				.get()
				.asFile
			val newMapping = mapping.parentFile.resolve(mapping.name + ".gen")
			val r8Provider = project.tasks
				.named<R8Task>("minify${variant.name.capitalized()}WithR8")
			val generateDebugMapping = project.tasks.register("generateDebugMapping") {
				this.description = "Generate mapping.txt for readable output"
				this.outputs.files(newMapping)
				this.outputs.upToDateWhen { false } // TODO inputs as jar files below
				this.doFirst {
					val r8 = r8Provider.get()
					println(project.files(r8.classes).files)
					println(project.files(r8.referencedClasses).files)
					println(project.files(r8.bootClasspath).files)
					val mapJars = project.files(r8.classes)
					val allJars = mapJars +
						project.files(r8.referencedClasses) +
						project.files(r8.bootClasspath)
					val loader: ClassLoader = java.net.URLClassLoader
						.newInstance(allJars.map { it.toURI().toURL() }.toTypedArray())
					val out = newMapping.printWriter()
					val output = { source: File, path: String, clazz: Class<*> ->
						if (false) println("${source}/${path}: ${clazz}")
						@Suppress("UNUSED_VARIABLE")
						val swapCase =
							org.gradle.internal.impldep.org.apache.commons.lang.StringUtils::swapCase
						val className = { s: String -> s.lowercase(Locale.ROOT) }
						val methodName = { s: String -> s }
						val fieldName = { s: String -> s }
						val getClassName = { c: Class<*> -> c.name }
						out.println("${getClassName(clazz)} -> ${className(getClassName(clazz))}:\n")
						clazz.declaredFields.forEach { field ->
							val type = field.type.canonicalName ?: getClassName(field.type)
							out.println("\t${type} ${field.name} -> ${fieldName(field.name)}")
						}
						clazz.declaredMethods.forEach { method ->
							val params = method.parameterTypes.joinToString(",") { it.canonicalName }
							val returnType = method.returnType.canonicalName //?: getClassName(field.type)
							out.println("\t${returnType} ${method.name}(${params}) -> ${methodName(method.name)}")
						}
					}
					mapJars.forEach { jar: File ->
						if (jar.isDirectory) {
							println("Dir: ${jar}")
							project.fileTree(jar).visit {
								val f = this
								if (!f.isDirectory && f.name.endsWith(".class")) {
									val className = f.relativePath.segments
										.joinToString(".")
										.removeSuffix(".class")
									val classObject = Class.forName(className, false, loader)
									output(jar, f.relativePath.pathString, classObject)
								}
							}
						} else {
							println("File: ${jar}")
							val zip = java.util.zip.ZipInputStream(FileInputStream(jar))
							generateSequence { zip.nextEntry }.forEach { f ->
								if (!f.isDirectory && f.name.endsWith(".class")) {
									val className = f.name
										.replace('/', '.')
										.removeSuffix(".class")
									val classObject = Class.forName(className, false, loader)
									output(jar, f.name, classObject)
								}
							}
						}
					}
					out.close()
				}
				this.doLast {
					r8Provider.get().mappingFile.set(newMapping)
				}
			}
			r8Provider.configure { dependsOn(generateDebugMapping) }
		}
	}
}
