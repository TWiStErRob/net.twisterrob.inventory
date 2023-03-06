import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.BaseVariantImpl
import com.android.build.gradle.internal.variant.BaseVariantData
import org.gradle.api.*
import proguard.gradle.ProGuardTask
import java.io.File
import java.io.FileInputStream
import java.util.Locale

// Currently not used, this version was just a test to try to generate a mapping from *.jar/**/*.class files
class GenerateDebugMappingPlugin : Plugin<Project> {

	override fun apply( project: Project) {
		val android = project.extensions.findByName("android") as AppExtension
		project.afterEvaluate {
			android.applicationVariants.all {
				val  variant: BaseVariant = this
				@Suppress("DEPRECATION")
				if (variant.obfuscation == null) return@all
				@Suppress("DEPRECATION")
				val mapping = variant.mappingFile
				val newMapping = mapping.parentFile.resolve(mapping.getName() + ".gen")
				val generateDebugMapping = project.tasks.register("generateDebugMapping") {
					val task = this
					task.description = "Generate mapping.txt for readable output"
					task.outputs.files(newMapping)
					task.outputs.upToDateWhen { false } // TODO inputs as jar files below
					task.doFirst {
						@Suppress("DEPRECATION")
						val pg = variant.obfuscation as ProGuardTask
						println(project.files(pg.inJarFiles).files)
						println(project.files(pg.libraryJarFiles).files)
						val variantData: BaseVariantData = (variant as BaseVariantImpl).variantData
						@Suppress("DEPRECATION")
						println(project.files(variantData.globalScope.getBootClasspath()).files)
						val mapJars = project.files(pg.inJarFiles)
						val allJars = mapJars +
								project.files(pg.libraryJarFiles) +
								@Suppress("DEPRECATION")
								project.files(variantData.globalScope.getBootClasspath())
						val loader: ClassLoader = java.net.URLClassLoader.newInstance(allJars.map { it.toURI().toURL() }.toTypedArray())
						val out = newMapping.printWriter()
						val output = { source: File, path: String, clazz: Class<*> ->
							if (false) println("${source}/${path}: ${clazz}")
							@Suppress("UNUSED_VARIABLE")
							val swapCase = org.gradle.internal.impldep.org.apache.commons.lang.StringUtils::swapCase
							val className = { s: String -> s.toLowerCase(Locale.ROOT) }
							val methodName = { s: String -> s }
							val fieldName = { s: String -> s }
							val getClassName = { c: Class<*> -> c.name }
							out.printf("%s -> %s:\n", getClassName(clazz), className(getClassName(clazz)))
							clazz.declaredFields.forEach { field ->
								out.printf("\t%s %s -> %s\n",
										field.type.canonicalName ?: getClassName(field.type),
										field.name,
										fieldName(field.name))
							}
							clazz.declaredMethods.forEach { method ->
								out.printf("\t%s %s(%s) -> %s\n",
										method.returnType.canonicalName, //?: getClassName(field.type),
										method.name,
										method.parameterTypes.joinToString(",") { it.canonicalName },
										methodName(method.name))
							}
						}
						mapJars.forEach { jar: File ->
							if (jar.isDirectory) {
								println("Dir: " + jar)
								project.fileTree(jar).visit { 
									val f = this
									if (!f.isDirectory && f.name.endsWith(".class")) {
										var className = f.relativePath.segments.joinToString(".")
										className = className.substring(0, className.length - ".class".length)
										output(jar, f.relativePath.pathString, Class.forName(className, false, loader))
									}
								}
							} else {
								println("File: " + jar)
								val zip = java.util.zip.ZipInputStream(FileInputStream(jar))
								generateSequence { zip.nextEntry }.forEach { f ->
									if (!f.isDirectory && f.name.endsWith(".class")) {
										var className = f.name.replace('/', '.')
										className = className.substring(0, className.length - ".class".length)
										output(jar, f.name, Class.forName(className, false, loader))
									}
								}
							}
						}
						out.close()
					}
					task.doLast {
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

private val BaseVariantImpl.variantData: BaseVariantData
	get() = this::class.java
		.getDeclaredMethod("getVariantData")
		.apply { isAccessible = true }
		.invoke(this) as BaseVariantData
