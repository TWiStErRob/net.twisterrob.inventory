import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.ddmlib.IShellEnabledDevice
import com.android.ddmlib.NullOutputReceiver
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.concurrent.TimeUnit.MILLISECONDS

class AndroidTestSetupPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		val android = project.extensions.findByName("android") as AppExtension
		android.testVariants.all {
			connectedInstrumentTestProvider.configure {
				configureAndroidTestTask(this as DeviceProviderInstrumentTestTask)
			}
		}
	}
}

private fun configureAndroidTestTask(task: DeviceProviderInstrumentTestTask) {
	task.doFirst {
		task.deviceProvider.run {
			init()
			devices.forEach { device ->
				device.adb("pm grant ${task.testData.applicationId} android.permission.SET_ANIMATION_SCALE")
				// Done in net.twisterrob.android.test.SystemAnimations
				//device.adb("settings put global window_animation_scale 0")
				//device.adb("settings put global transition_animation_scale 0")
				//device.adb("settings put global animator_duration_scale 0")
				// Used by net.twisterrob.android.test.DeviceUnlocker
				device.adb("pm grant ${task.testData.applicationId} android.permission.DISABLE_KEYGUARD")
				// STOPSHIP reduce flakyness
				//device.adb("pm grant ${task.testData.applicationId} android.permission.WRITE_SECURE_SETTINGS")
				//device.adb("settings put secure long_press_timeout 1000")
			}
			terminate()
		}
	}
}

private fun IShellEnabledDevice.adb(cmd: String) {
	executeShellCommand(cmd, NullOutputReceiver(), 0, MILLISECONDS)
}
