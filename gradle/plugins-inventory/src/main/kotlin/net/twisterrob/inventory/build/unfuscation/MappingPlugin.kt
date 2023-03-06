package net.twisterrob.inventory.build.unfuscation

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.api.TestedVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.register

class MappingPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val android = project.extensions.findByName("android") as AppExtension
		android.applicationVariants.all {
			val variant: ApkVariant = this
			@Suppress("DEPRECATION")
			val obfuscateTask = variant.obfuscation
			val skipReason = mutableListOf<String>()
			if (obfuscateTask == null) {
				skipReason += "not obfuscated"
			}
			if (!variant.buildType.isDebuggable) {
				skipReason += "not debuggable"
			}
			if (variant is TestedVariant && variant.testVariant != null) {
				skipReason += "tested"
			}
			if (!skipReason.isEmpty()) {
				project.logger.info("Skipping unfuscation of {} because it is {}", variant.name, skipReason)
				return@all
			}

			val unfuscateTask = project.tasks.register<UnfuscateTask>("${obfuscateTask.name}Unfuscate") {
				println(this)
				val task = this
				task.obfuscateTask = obfuscateTask
				@Suppress("DEPRECATION")
				task.mapping = variant.mappingFile
				task.newMapping = task.mapping.parentFile.resolve("unmapping.txt")
				task.dependsOn(obfuscateTask)
			}
			@Suppress("DEPRECATION")
			(variant.dex as Task).dependsOn(unfuscateTask)
		}
	}
}
