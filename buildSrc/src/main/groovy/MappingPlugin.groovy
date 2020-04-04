import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.api.TestedVariant
import org.gradle.api.*
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.*

class MappingPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		def android = project.extensions.findByName("android") as AppExtension
		android.applicationVariants.all { ApkVariant variant ->
			Task obfuscateTask = variant.obfuscation
			def skipReason = [ ]
			if (obfuscateTask == null) {
				skipReason += "not obfuscated"
			}
			if (!variant.buildType.debuggable) {
				skipReason += "not debuggable"
			}
			if (variant instanceof TestedVariant && variant.testVariant != null) {
				skipReason += "tested"
			}
			if (!skipReason.isEmpty()) {
				project.logger.info("Skipping unfuscation of {} because it is {}", variant.name, skipReason)
				return
			}

			def unfuscateTask = project.tasks.register("${obfuscateTask.name}Unfuscate", UnfuscateTask) { UnfuscateTask task ->
				task.obfuscateTask = obfuscateTask
				task.mapping = variant.mappingFile
				task.newMapping = new File(task.mapping.parentFile, "unmapping.txt")
				task.dependsOn(obfuscateTask)
			}
			(variant.dex as Task).dependsOn(unfuscateTask)
		}
	}
}

@SuppressWarnings("UnnecessaryQualifiedReference")
class UnfuscateTask extends DefaultTask {
	
	UnfuscateTask() {
		outputs.upToDateWhen { mapping.lastModified() <= newMapping.lastModified() }
	}

	@Input
	Task obfuscateTask

	@InputFile
	File mapping
	
	@OutputFile
	File newMapping

	@TaskAction
	def unfuscate() {
		def configField = proguard.gradle.ProGuardTask.class.getDeclaredField("configuration")
		configField.accessible = true
		def config = configField.get(obfuscateTask) as proguard.Configuration
		if (!config.obfuscate) {
			return // nothing to unfuscate when -dontobfuscate
		}

		java.nio.file.Files.copy(mapping.toPath(), new File(mapping.parentFile, "mapping.txt.bck").toPath(),
				java.nio.file.StandardCopyOption.REPLACE_EXISTING)
		logger.info("Writing new mapping file: {}", newMapping)
		new Mapping(mapping).remap(newMapping)

		logger.info("Re-executing {} with new mapping...", obfuscateTask.name)
		config.applyMapping = newMapping // use our re-written mapping file
		//config.note = [ '**' ] // -dontnote **, it was noted in the first run

		def loggingManager = getLogging()
		// lower level of logging to prevent duplicate output
		loggingManager.captureStandardOutput(LogLevel.WARN)
		loggingManager.captureStandardError(LogLevel.WARN)
		new proguard.ProGuard(config).execute()
	}
}

@SuppressWarnings("UnnecessaryQualifiedReference")
class Mapping {
	private static java.util.regex.Pattern MAPPING_PATTERN =
			~/^(?<member>    )?(?<location>\d+:\d+:)?(?:(?<type>.*?) )?(?<name>.*?)(?:\((?<args>.*?)\))?(?: -> )(?<obfuscated>.*?)(?<class>:?)$/ 
	private static int MAPPING_PATTERN_OBFUSCATED_INDEX = 6

	private final File source
	Mapping(File source) {
		this.source = source
	}

	void remap(File target) {
		target.withWriter { source.eachLine Mapping.&processLine.curry(it) }
	}

	// CONSIDER supporting -applymapping by using the names from the original -applymapping file
	private static void processLine(Writer out, String line, int num) {
		java.util.regex.Matcher m = MAPPING_PATTERN.matcher(line)
		if (!m.find()) {
			throw new IllegalArgumentException("Line #${num} is not recognized: ${line}")
		}
		try {
			def originalName = m.group("name")
			def obfuscatedName = m.group("obfuscated")
			def newName = originalName.equals(obfuscatedName) ? obfuscatedName : unfuscate(originalName, obfuscatedName)
			out.write(line.substring(0, m.start(MAPPING_PATTERN_OBFUSCATED_INDEX)))
			out.write(newName)
			out.write(line.substring(m.end(MAPPING_PATTERN_OBFUSCATED_INDEX)))
			out.write('\n')
		} catch (Exception ex) {
			StringBuilder sb = new StringBuilder("Line #${num} failed: ${line}\n")
			0.upto(m.groupCount()) { Number it -> sb.append("Group #${it}: '${m.group(it.intValue())}'\n") }
			throw new IllegalArgumentException(sb.toString(), ex)
		}
	}

	private static String unfuscate(String original, String obfuscated) {
		// reassemble the names with something readable, but still breaking changes
		def origName = getName(original)
		def obfName = getName(obfuscated)
		obfName = obfName.equals(origName) ? "" : obfName
		return getPackage(original) + origName + '_' + obfName
	}
	private static String getPackage(String name) {
		int lastDot = name.lastIndexOf('.')
		return lastDot < 0 ? "" : name.substring(0, lastDot + 1)
	}
	private static String getName(String name) {
		return name.substring(name.lastIndexOf('.') + 1)
	}
}
