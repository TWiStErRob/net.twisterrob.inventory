plugins {
	id("net.twisterrob.android-library")
	id("net.twisterrob.inventory.build.android.base")
	id("net.twisterrob.inventory.build.allprojects")
}

afterEvaluate {
	@Suppress("DEPRECATION") // REPORT cannot replace with new interface, missing methods
	val androidTest = project.android.sourceSets["androidTest"] as com.android.build.gradle.api.AndroidSourceSet
	if (androidTest.java.getSourceFiles().isEmpty) {
		logger.info("Disabling AndroidTest tasks in ${project.path} as it has no sources in ${androidTest.java.srcDirs}")
		tasks.whenTaskAdded {
			if (this.name.contains("AndroidTest")) {
				this.enabled = false
			}
		}
	}
}
