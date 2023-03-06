package net.twisterrob.inventory.build.tests

import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.build.gradle.internal.testing.SimpleTestRunner
import com.android.build.gradle.internal.testing.StaticTestData
import com.android.builder.testing.api.DeviceConnector
import com.android.ddmlib.IShellEnabledDevice
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.android.ide.common.process.ProcessExecutor
import com.android.ide.common.workers.ExecutorServiceAdapter
import java.io.File
import java.lang.reflect.Proxy

/**
 * Just for readability, otherwise private interface in [DeviceProviderInstrumentTestTask].
 */
typealias TestRunnerFactory = Any

/**
 * Shorthand for configuration block to be executed
 *  * after `V/ddms: execute 'pm install ...`
 *  * before `ddms: execute: running am instrument ...`
 * so that the install instrumentation APK can be granted permissions.
 */
typealias PerDeviceSetupCallback = IShellEnabledDevice.(packageName: String) -> Unit

private var DeviceProviderInstrumentTestTask.testRunnerFactory: TestRunnerFactory
	get() = DeviceProviderInstrumentTestTask::class.java
		.getDeclaredField("testRunnerFactory")
		.getValue(this)
	set(value) {
		DeviceProviderInstrumentTestTask::class.java
			.getDeclaredField("testRunnerFactory")
			.setValue(this, value)
	}

fun DeviceProviderInstrumentTestTask.replaceTestRunnerFactory(configure: PerDeviceSetupCallback) {
	this.testRunnerFactory =
		replaceTestRunnerFactory(this.testRunnerFactory, this.executorServiceAdapter, configure)
}

@Suppress("PrivateApi") // REPORT Android lint false positive, nothing to do with buildSrc.
private fun replaceTestRunnerFactory(
	originalFactory: TestRunnerFactory,
	executor: ExecutorServiceAdapter,
	configure: PerDeviceSetupCallback
): TestRunnerFactory {
	// The implementation should be as commented below, but TestRunnerFactory is a private interface,
	// so using JVM Proxy to implement it is the only way.
	//return object : DeviceProviderInstrumentTestTask.TestRunnerFactory {
	//	override fun build(splitSelectExec: File?, processExecutor: ProcessExecutor): TestRunner =
	//		MySimpleTestRunner(splitSelectExec, processExecutor, executor, block)
	//}
	@Suppress("LocalVariableName", "VariableNaming")
	val TestRunnerFactory = Class.forName(
		"com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask\$TestRunnerFactory"
	)
	return Proxy.newProxyInstance(TestRunnerFactory.classLoader, arrayOf(TestRunnerFactory))
	{ _, method, args ->
		if (method.name == "build") {
			ConfiguringTestRunner(args[0] as File?, args[1] as ProcessExecutor, executor, configure)
		} else {
			// need to expose even public interface methods
			method.isAccessible = true
			@Suppress("SpreadOperator") // Have to use it, no other way to call this method.
			method.invoke(originalFactory, *args)
		}
	}
}

private class ConfiguringTestRunner(
	splitSelectExec: File?,
	processExecutor: ProcessExecutor,
	executor: ExecutorServiceAdapter,
	private val configure: PerDeviceSetupCallback
) : SimpleTestRunner(splitSelectExec, processExecutor, executor) {

	override fun createRemoteAndroidTestRunner(
		testData: StaticTestData,
		@Suppress("UnstableApiUsage") device: DeviceConnector
	): RemoteAndroidTestRunner =
		ConfiguringRemoteAndroidTestRunner(
			testData.applicationId,
			testData.instrumentationRunner,
			device,
			configure
		)
}

private class ConfiguringRemoteAndroidTestRunner(
	packageName: String,
	runnerName: String,
	private val remoteDevice: IShellEnabledDevice,
	private val configure: PerDeviceSetupCallback
) : RemoteAndroidTestRunner(packageName, runnerName, remoteDevice) {

	override fun run(listeners: MutableCollection<ITestRunListener>?) {
		remoteDevice.configure(packageName)
		super.run(listeners)
	}
}
