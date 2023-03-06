import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.internal.api.TestedVariant
import org.gradle.api.*

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
