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

			@Suppress("DEPRECATION") // TODO don't know where to get this from. Retry in AGP 7.4 or 8.x
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
			if (skipReason.isNotEmpty()) {
				project.logger.info(
					"Skipping unfuscation of {} because it is {}",
					variant.name,
					skipReason
				)
				return@all
			}

			val unfuscateTask =
				project.tasks.register<UnfuscateTask>("${obfuscateTask.name}Unfuscate") {
					this.obfuscateTask = obfuscateTask
					this.mapping.fileProvider(variant.mappingFileProvider.map { it.singleFile })
					this.newMapping.fileProvider(this.mapping.map { it.asFile.parentFile.resolve("unmapping.txt") })
					this.dependsOn(obfuscateTask)
				}

			@Suppress("DEPRECATION")
			(variant.dex as Task).dependsOn(unfuscateTask)
		}
	}
}
