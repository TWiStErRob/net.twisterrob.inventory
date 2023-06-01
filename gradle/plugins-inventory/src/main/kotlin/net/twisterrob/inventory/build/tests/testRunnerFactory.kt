package net.twisterrob.inventory.build.tests

import com.android.build.gradle.internal.process.GradleProcessExecutor
import com.android.build.gradle.internal.tasks.DelegatingTestRunnerFactory
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask.TestRunnerFactory
import com.android.build.gradle.internal.testing.SimpleTestRunner
import com.android.build.gradle.internal.testing.StaticTestData
import com.android.build.gradle.internal.testing.TestRunner
import com.android.build.gradle.internal.testing.utp.UtpTestResultListener
import com.android.builder.testing.api.DeviceConnector
import com.android.ddmlib.IShellEnabledDevice
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.android.ide.common.process.ProcessExecutor
import com.android.ide.common.workers.ExecutorServiceAdapter
import org.gradle.workers.WorkerExecutor
import java.io.File

/**
 * Shorthand for configuration block to be executed
 *  * after `V/ddms: execute 'pm install ...`
 *  * before `ddms: execute: running am instrument ...`
 * so that the install instrumentation APK can be granted permissions.
 */
typealias PerDeviceCallback = IShellEnabledDevice.(packageName: String) -> Unit

/**
 * Note, this access is for [DeviceProviderInstrumentTestTask.getTestRunnerFactory],
 * but that is an `abstract` method, which will be implemented by Gradle.
 * This means we have to use `this::class.java` to get the reference to the `_Decorated` class.
 */
@Suppress("UnusedPrivateMember")
private var DeviceProviderInstrumentTestTask.testRunnerFactoryAccess: TestRunnerFactory
	get() = this::class.java
		.getDeclaredField("__testRunnerFactory__")
		.getValue(this)
	set(value) {
		this::class.java
			.getDeclaredField("__testRunnerFactory__")
			.setValue(this, value)
	}

fun DeviceProviderInstrumentTestTask.replaceTestRunnerFactory(before: PerDeviceCallback, after: PerDeviceCallback) {
	this.testRunnerFactoryAccess = replaceTestRunnerFactory(this.testRunnerFactory, before, after)
}

private fun DeviceProviderInstrumentTestTask.replaceTestRunnerFactory(
	originalFactory: TestRunnerFactory,
	before: PerDeviceCallback,
	after: PerDeviceCallback,
): TestRunnerFactory =
	@Suppress("UnstableApiUsage")
	object : DelegatingTestRunnerFactory(originalFactory) {
		// See com.android.build.gradle.internal.testing.utp.shouldEnableUtp
		// See com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask.TestRunnerFactory.createTestRunner
		// Assumes Property android.experimental.androidTest.useUnifiedTestPlatform = false
		// (Default true at 7.4.2, see com.android.build.gradle.options.BooleanOption.ANDROID_TEST_USES_UNIFIED_TEST_PLATFORM)
		// Assumes DSL: android.testOptions.execution = HOST
		// (Default HOST at 7.4.2, see com.android.build.gradle.internal.dsl.TestOptions)
		// Assumes Property: android.androidTest.shardBetweenDevices = false
		// (Default false at 7.4.2, see com.android.build.gradle.options.BooleanOption.ENABLE_TEST_SHARDING)
		// Assumes DSL: android.testOptions.emulatorSnapshots.enableForTestFailures = false
		// (Default false at 7.4.2, see com.android.build.gradle.internal.dsl.EmulatorSnapshots.enableForTestFailures)
		override fun createTestRunner(
			workerExecutor: WorkerExecutor,
			executorServiceAdapter: ExecutorServiceAdapter,
			utpTestResultListener: UtpTestResultListener?,
		): TestRunner {
			val originalTestRunner = super.createTestRunner(
				workerExecutor,
				executorServiceAdapter,
				utpTestResultListener,
			)
			if (originalTestRunner !is SimpleTestRunner) {
				logger.error("Test runner is not a SimpleTestRunner: ${originalTestRunner}, post-deploy setup is not applied!")
				return originalTestRunner
			}
			return ConfiguringTestRunner(
				buildTools.splitSelectExecutable().orNull,
				GradleProcessExecutor(execOperations::exec),
				executorServiceAdapter,
				before,
				after,
			)
		}
	}

private class ConfiguringTestRunner(
	splitSelectExec: File?,
	processExecutor: ProcessExecutor,
	executor: ExecutorServiceAdapter,
	private val before: PerDeviceCallback,
	private val after: PerDeviceCallback,
) : SimpleTestRunner(splitSelectExec, processExecutor, executor) {

	override fun createRemoteAndroidTestRunner(
		testData: StaticTestData,
		@Suppress("UnstableApiUsage") device: DeviceConnector
	): RemoteAndroidTestRunner =
		ConfiguringRemoteAndroidTestRunner(
			testData.applicationId,
			testData.instrumentationRunner,
			device,
			before,
			after,
		)
}

private class ConfiguringRemoteAndroidTestRunner(
	packageName: String,
	runnerName: String,
	private val remoteDevice: IShellEnabledDevice,
	private val before: PerDeviceCallback,
	private val after: PerDeviceCallback,
) : RemoteAndroidTestRunner(packageName, runnerName, remoteDevice) {

	override fun run(listeners: MutableCollection<ITestRunListener>?) {
		remoteDevice.before(packageName)
		super.run(listeners)
		remoteDevice.after(packageName)
	}
}
