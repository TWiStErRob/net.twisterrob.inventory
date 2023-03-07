package net.twisterrob.inventory.build.tests.upgrade

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import net.twisterrob.gradle.android.androidComponents
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

class UpgradeTestPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		(project.androidComponents as ApplicationAndroidComponentsExtension).onVariants { variant ->
			if (variant.androidTest == null) return@onVariants
			val upgradeTest = project.tasks.register<UpgradeTestTask>("upgradeTest") {
				enabled = false // TODO not working since AGP 3.3/3.4
				testedVariant.set(variant)
				val connectedAndroidTest = project.tasks.named("connectedAndroidTest")

				@Suppress("UNCHECKED_CAST")
				val instrument = connectedAndroidTest.get().dependsOn.single()
					as TaskProvider<DeviceProviderInstrumentTestTask>
				instrumentTestTask.set(instrument)

				dependsOn("assembleDebug")
				dependsOn("assembleAndroidTest")
			}
			project.tasks.named("connectedCheck").configure {
				dependsOn(upgradeTest)
			}
		}
	}
}
