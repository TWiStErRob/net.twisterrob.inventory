package net.twisterrob.inventory.build.unfuscation

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.R8Task
import net.twisterrob.gradle.android.androidComponents
import net.twisterrob.inventory.build.dsl.android
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

class MappingPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		(project.androidComponents as ApplicationAndroidComponentsExtension).onVariants { variant ->
			val skipReason = mutableListOf<String>()
			@Suppress("UnstableApiUsage")
			if (!variant.isMinifyEnabled) {
				skipReason += "not obfuscated"
			}
			val buildType =
				(project.android as ApplicationExtension).buildTypes[variant.buildType!!]
			if (!buildType.isDebuggable) {
				skipReason += "not debuggable"
			}
			if (variant.androidTest != null) {
				skipReason += "tested"
			}
			if (skipReason.isNotEmpty()) {
				project.logger.info(
					"Skipping unfuscation of {} because it is {}",
					variant.name,
					skipReason
				)
				return@onVariants
			}

			val obfuscateTask: TaskProvider<R8Task> =
				project.tasks.named<R8Task>("minify${variant.name.replaceFirstChar(Char::uppercase)}WithR8")
			val unfuscateTask =
				project.tasks.register<UnfuscateTask>("${obfuscateTask.name}Unfuscate") {
					this.obfuscateTask.set(obfuscateTask)
					this.dependsOn(obfuscateTask)
				}

			// TODO Figure out how to wire this.
			variant.artifacts.use(unfuscateTask)
				.wiredWithFiles(UnfuscateTask::mapping, UnfuscateTask::newMapping)
			//  .toTransform(SingleArtifact.OBFUSCATION_MAPPING_FILE)
		}
	}
}
