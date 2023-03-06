package net.twisterrob.inventory.build.unfuscation

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class UnfuscateTask : DefaultTask() {

	init {
		outputs.upToDateWhen { mapping.lastModified() <= newMapping.lastModified() }
	}

	@get:Input
	abstract var obfuscateTask: Task

	@get:InputFile
	@get:PathSensitive(PathSensitivity.RELATIVE)
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
