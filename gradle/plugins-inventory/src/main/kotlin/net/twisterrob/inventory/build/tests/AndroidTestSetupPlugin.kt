package net.twisterrob.inventory.build.tests

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.ddmlib.IShellEnabledDevice
import com.android.ddmlib.NullOutputReceiver
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.concurrent.TimeUnit.MILLISECONDS

class AndroidTestSetupPlugin : Plugin<Project> {

	private lateinit var project: Project

	override fun apply(project: Project) {
		this.project = project
		project.androidApp.testVariants.all {
			connectedInstrumentTestProvider.configure {
				runBeforeAndroidTest(this as DeviceProviderInstrumentTestTask) { packageName ->
					// Used by net.twisterrob.android.test.SystemAnimations
					adbShell("pm grant ${packageName} android.permission.SET_ANIMATION_SCALE")
					// to set these:
					//adbShell("settings put global window_animation_scale 0")
					//adbShell("settings put global transition_animation_scale 0")
					//adbShell("settings put global animator_duration_scale 0")

					// Used by net.twisterrob.android.test.DeviceUnlocker
					adbShell("pm grant ${packageName} android.permission.DISABLE_KEYGUARD")

					// Used by org.junit.rules.TemporaryFolder (only required 26-29)
					adbShell("pm grant ${packageName} android.permission.READ_EXTERNAL_STORAGE")
					adbShell("pm grant ${packageName} android.permission.WRITE_EXTERNAL_STORAGE")

					// TODO move to TestRule?
					//adbShell("pm grant ${packageName} android.permission.WRITE_SECURE_SETTINGS")
					adbShell("settings put secure long_press_timeout 1500")
				}
			}
		}
	}

	private fun IShellEnabledDevice.adbShell(command: String) {
		project.logger.info("Executing `adb shell ${command}`")
		executeShellCommand(command, NullOutputReceiver(), 0, MILLISECONDS)
	}
}

private val Project.androidApp: AppExtension
	get() = this.extensions.findByName("android") as AppExtension

private fun runBeforeAndroidTest(
	task: DeviceProviderInstrumentTestTask,
	block: IShellEnabledDevice.(packageName: String) -> Unit
) {
	// Originally I tried the below doFirst block.
	// Even though that's the latest public injection point,
	// it's still too early, because the instrumentation APK is installed.
	// Need to be before `ddms: execute: running am instrument ...`,
	// but after `V/ddms: execute 'pm install ...`.
	//task.doFirst {
	//	task.deviceProvider.run {
	//		init()
	//		try {
	//			devices.forEach { device ->
	//				device.configure(task.testData.applicationId)
	//			}
	//		} finally {
	//			terminate()
	//		}
	//	}
	//}

	// Replace the test runner factory to replace the test runner,
	// which replaces RemoteAndroidTestRunner so the block can be injected at the actual execution.
	task.replaceTestRunnerFactory(block)
}
