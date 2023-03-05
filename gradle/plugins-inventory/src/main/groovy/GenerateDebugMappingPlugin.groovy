import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.BaseVariantImpl
import com.android.build.gradle.internal.variant.BaseVariantData
import org.gradle.api.*
import proguard.gradle.ProGuardTask

// Currently not used, this version was just a test to try to generate a mapping from *.jar/**/*.class files
@SuppressWarnings(["UnnecessaryQualifiedReference", "GrDeprecatedAPIUsage"])
class GenerateDebugMappingPlugin implements Plugin<Project> {

	@Override void apply(Project project) {
		def android = project.extensions.findByName("android") as AppExtension
		project.afterEvaluate {
			android.applicationVariants.all { BaseVariant variant ->
				if (!variant.obfuscation) return
				File mapping = variant.mappingFile
				File newMapping = new File(mapping.parentFile, mapping.getName() + ".gen")
				def generateDebugMapping = project.tasks.register("generateDebugMapping") { task ->
					task.description = "Generate mapping.txt for readable output"
					task.outputs.files newMapping
					task.outputs.upToDateWhen { false } // TODO inputs as jar files below
					task.doFirst {
						ProGuardTask pg = variant.obfuscation as ProGuardTask
						println project.files(pg.inJarFiles).files
						println project.files(pg.libraryJarFiles).files
						BaseVariantData variantData = (variant as BaseVariantImpl).variantData
						println project.files(variantData.globalScope.getBootClasspath()).files
						def mapJars = project.files(pg.inJarFiles)
						def allJars = mapJars +
								project.files(pg.libraryJarFiles) + 
								project.files(variantData.globalScope.getBootClasspath())
						ClassLoader loader = java.net.URLClassLoader.newInstance(allJars.toList()*.toURI()*.toURL() as URL[])
						PrintWriter out = new PrintWriter(newMapping)
						def output = { File source, String path, Class<?> clazz ->
							//println "${source}/${path}: ${clazz}"
							def swapCase = org.gradle.internal.impldep.org.apache.commons.lang.StringUtils.&swapCase
							def className = { String s -> s.toLowerCase() }
							def methodName = { String s -> s }
							def fieldName = { String s -> s }
							def getClassName = { Class<?> c -> java.lang.Class.getDeclaredMethod("getName").invoke(c) as String }
							out.printf("%s -> %s:\n", getClassName(clazz), className(getClassName(clazz)))
							for (java.lang.reflect.Field field : clazz.declaredFields) {
								out.printf("\t%s %s -> %s\n",
										field.type.canonicalName ?: getClassName(field.type),
										field.name,
										fieldName(field.name))
							}
							for (java.lang.reflect.Method method : clazz.declaredMethods) {
								out.printf("\t%s %s(%s) -> %s\n",
										method.returnType.canonicalName, //?: getClassName(field.type),
										method.name,
										method.parameterTypes*.canonicalName.join(","),
										methodName(method.name))
							}
						}
						mapJars.each { File jar ->
							if (jar.directory) {
								println "Dir: " + jar
								project.fileTree(jar).visit { org.gradle.api.file.FileVisitDetails f ->
									if (!f.directory && f.name.endsWith(".class")) {
										String className = f.relativePath.segments.join(".")
										className = className.substring(0, className.length() - ".class".length())
										output(jar, f.relativePath.pathString, Class.forName(className, false, loader))
									}
								}
							} else {
								println "File: " + jar
								java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(
										new FileInputStream(jar))
								for (java.util.zip.ZipEntry f = zip.getNextEntry(); f != null; f = zip.getNextEntry()) {
									if (!f.directory && f.name.endsWith(".class")) {
										String className = f.name.replace('/', '.')
										className = className.substring(0, className.length() - ".class".length())
										output(jar, f.name, Class.forName(className, false, loader))
									}
								}
							}
						}
						out.close()
					}
					task.doLast {
						ProGuardTask pg = variant.obfuscation as ProGuardTask
						pg.applymapping(newMapping)
					}
				}
				variant.obfuscation.dependsOn generateDebugMapping
			}
		}
	}
}
