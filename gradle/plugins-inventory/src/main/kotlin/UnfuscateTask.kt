import org.gradle.api.*
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.*

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
