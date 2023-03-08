package com.android.build.gradle.internal.tasks

import com.android.build.gradle.internal.BuildToolsExecutableInput
import com.android.build.gradle.internal.SdkComponentsBuildService
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask.TestRunnerFactory
import com.android.build.gradle.internal.testing.TestRunner
import com.android.build.gradle.internal.testing.utp.RetentionConfig
import com.android.build.gradle.internal.testing.utp.UtpDependencies
import com.android.build.gradle.internal.testing.utp.UtpTestResultListener
import com.android.builder.model.TestOptions.Execution
import com.android.ide.common.workers.ExecutorServiceAdapter
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkerExecutor

@Suppress("UsePropertyAccessSyntax", "TooManyFunctions")
abstract class DelegatingTestRunnerFactory(
	@Internal
	protected val delegate: TestRunnerFactory
) : TestRunnerFactory() {

	@Suppress("UnstableApiUsage")
	override fun createTestRunner(
		workerExecutor: WorkerExecutor,
		executorServiceAdapter: ExecutorServiceAdapter,
		utpTestResultListener: UtpTestResultListener?
	): TestRunner =
		delegate.createTestRunner(workerExecutor, executorServiceAdapter, utpTestResultListener)

	override fun getExecOperations(): ExecOperations =
		delegate.getExecOperations()

	override fun getUnifiedTestPlatform(): Property<Boolean> =
		delegate.getUnifiedTestPlatform()

	override fun getIsUtpLoggingEnabled(): Property<Boolean> =
		delegate.getIsUtpLoggingEnabled()

	override fun getShardBetweenDevices(): Property<Boolean> =
		delegate.getShardBetweenDevices()

	override fun getNumShards(): Property<Int> =
		delegate.getNumShards()

	override fun getUninstallIncompatibleApks(): Property<Boolean> =
		delegate.getUninstallIncompatibleApks()

	override fun getExecutionEnum(): Property<Execution> =
		delegate.getExecutionEnum()

	override fun getRetentionConfig(): Property<RetentionConfig> =
		delegate.getRetentionConfig()

	override fun getSdkBuildService(): Property<SdkComponentsBuildService> =
		delegate.getSdkBuildService()

	override fun getUtpDependencies(): UtpDependencies =
		delegate.getUtpDependencies()

	override fun getConnectedCheckDeviceSerials(): ListProperty<String> =
		delegate.getConnectedCheckDeviceSerials()

	override fun getDeviceSerialValues(): ListProperty<String> =
		delegate.getDeviceSerialValues()

	override fun getBuildTools(): BuildToolsExecutableInput =
		delegate.getBuildTools()
}
