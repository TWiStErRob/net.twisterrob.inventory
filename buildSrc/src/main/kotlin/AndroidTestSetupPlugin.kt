import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.builder.testing.ConnectedDeviceProvider
import com.android.builder.testing.SimpleTestRunner
import com.android.builder.testing.TestData
import com.android.builder.testing.TestRunner
import com.android.ddmlib.IShellEnabledDevice
import com.android.ddmlib.NullOutputReceiver
import net.twisterrob.gradle.androidApp
import net.twisterrob.gradle.replaceTestRunnerFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.concurrent.TimeUnit.MILLISECONDS

class AndroidTestSetupPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.androidApp.testVariants.all {
			connectedInstrumentTestProvider.configure {
				runBeforeAndroidTest(this as DeviceProviderInstrumentTestTask) { packageName ->
					// Used by net.twisterrob.android.test.SystemAnimations
					adb("pm grant $packageName android.permission.SET_ANIMATION_SCALE")
					// to set these:
					//adb("settings put global window_animation_scale 0")
					//adb("settings put global transition_animation_scale 0")
					//adb("settings put global animator_duration_scale 0")

					// Used by net.twisterrob.android.test.DeviceUnlocker
					adb("pm grant $packageName android.permission.DISABLE_KEYGUARD")

					// TODO move to TestRule?
					//adb("pm grant ${packageName} android.permission.WRITE_SECURE_SETTINGS")
					adb("settings put secure long_press_timeout 1500")
				}
			}
		}
	}
}

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

private fun IShellEnabledDevice.adb(cmd: String) {
	executeShellCommand(cmd, NullOutputReceiver(), 0, MILLISECONDS)
}
