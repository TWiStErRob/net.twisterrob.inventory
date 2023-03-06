import org.gradle.api.*
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.*
import java.io.File

@SuppressWarnings("UnnecessaryQualifiedReference")
abstract class UnfuscateTask : DefaultTask() {

	init {
		outputs.upToDateWhen { mapping.lastModified() <= newMapping.lastModified() }
	}

	@get:Input
	abstract var obfuscateTask: Task

	@get:InputFile
	abstract var mapping: File

	@get:OutputFile
	abstract var newMapping: File

	@TaskAction
	fun unfuscate() {
		val configField = proguard.gradle.ProGuardTask::class.java.getDeclaredField("configuration")
		configField.isAccessible = true
		val config = configField.get(obfuscateTask) as proguard.Configuration
		if (!config.obfuscate) {
			return // nothing to unfuscate when -dontobfuscate
		}

		java.nio.file.Files.copy(mapping.toPath(), mapping.parentFile.resolve("mapping.txt.bck").toPath(),
				java.nio.file.StandardCopyOption.REPLACE_EXISTING)
		logger.info("Writing new mapping file: {}", newMapping)
		Mapping(mapping).remap(newMapping)

		logger.info("Re-executing {} with new mapping...", obfuscateTask.name)
		config.applyMapping = newMapping // use our re-written mapping file
		//config.note = [ '**' ] // -dontnote **, it was noted in the first run

		val loggingManager = getLogging()
		// lower level of logging to prevent duplicate output
		loggingManager.captureStandardOutput(LogLevel.WARN)
		loggingManager.captureStandardError(LogLevel.WARN)
		proguard.ProGuard(config).execute()
	}
}
