package net.twisterrob.inventory.build.unfuscation

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import net.twisterrob.gradle.android.androidComponents
import org.gradle.api.Plugin
import org.gradle.api.Project
import proguard.gradle.ProGuardTask
import java.io.File
import java.io.FileInputStream
import java.util.Locale

// Currently not used, this version was just a test to try to generate a mapping from *.jar/**/*.class files
@Suppress("UnstableApiUsage")
class GenerateDebugMappingPlugin : Plugin<Project> {

	@Suppress("LongMethod")
	override fun apply(project: Project) {
		val android = project.extensions.findByName("android") as AppExtension
		project.afterEvaluate {
			android.applicationVariants.all {
				val variant: BaseVariant = this
				@Suppress("DEPRECATION")
				if (variant.obfuscation == null) return@all
				val mapping = variant.mappingFileProvider.get().singleFile
				val newMapping = mapping.parentFile.resolve(mapping.name + ".gen")
				val generateDebugMapping = project.tasks.register("generateDebugMapping") {
					this.description = "Generate mapping.txt for readable output"
					this.outputs.files(newMapping)
					this.outputs.upToDateWhen { false } // TODO inputs as jar files below
					this.doFirst {
						@Suppress("DEPRECATION")
						val pg = variant.obfuscation as ProGuardTask
						println(project.files(pg.inJarFiles).files)
						println(project.files(pg.libraryJarFiles).files)
						println(project.files(project.androidComponents.sdkComponents.bootClasspath).files)
						val mapJars = project.files(pg.inJarFiles)
						val allJars = mapJars +
							project.files(pg.libraryJarFiles) +
							project.files(project.androidComponents.sdkComponents.bootClasspath)
						val loader: ClassLoader = java.net.URLClassLoader
							.newInstance(allJars.map { it.toURI().toURL() }.toTypedArray())
						val out = newMapping.printWriter()
						val output = { source: File, path: String, clazz: Class<*> ->
							if (false) println("${source}/${path}: ${clazz}")
							@Suppress("UNUSED_VARIABLE")
							val swapCase =
								org.gradle.internal.impldep.org.apache.commons.lang.StringUtils::swapCase
							val className = { s: String -> s.toLowerCase(Locale.ROOT) }
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
						@Suppress("DEPRECATION")
						val pg = variant.obfuscation as ProGuardTask
						pg.applymapping(newMapping)
					}
				}
				@Suppress("DEPRECATION")
				variant.obfuscation.dependsOn(generateDebugMapping)
			}
		}
	}
}
