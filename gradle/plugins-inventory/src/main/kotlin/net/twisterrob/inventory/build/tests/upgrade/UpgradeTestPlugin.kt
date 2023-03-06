package net.twisterrob.inventory.build.tests.upgrade

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class UpgradeTestPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val upgradeTest = project.tasks.register<UpgradeTestTask>("upgradeTest") {
			enabled = false // TODO not working since AGP 3.3/3.4
			dependsOn("assembleDebug")
			dependsOn("assembleAndroidTest")
		}
		project.tasks.named("connectedCheck").configure {
			dependsOn(upgradeTest)
		}
	}
}
