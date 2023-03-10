package net.twisterrob.inventory.build.tests.upgrade

import com.android.build.api.artifact.Artifacts
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.component.impl.ComponentImpl
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.Variant
import com.android.build.gradle.internal.services.VariantServices
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.build.gradle.internal.test.report.ReportType
import com.android.build.gradle.internal.test.report.ResilientTestReport
import com.android.build.gradle.internal.testing.ConnectedDevice
import com.android.build.gradle.internal.testing.TestData
import com.android.builder.testing.api.DeviceConnector
import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.android.utils.FileUtils
import com.android.utils.StdLogger
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("UnstableApiUsage")
@DisableCachingByDefault(because = "Lots of external factors")
abstract class UpgradeTestTask : DefaultTask() {

	@get:Input
	abstract val testedVariant: Property<ApplicationVariant>

	@get:Input
	abstract val instrumentTestTask: Property<DeviceProviderInstrumentTestTask>

	@get:InputFile
	@get:PathSensitive(PathSensitivity.NONE)
	abstract val adb: RegularFileProperty

	@Suppress("LongMethod") // Will be split up when I make it work again.
	@TaskAction
	fun upgradeTest() {
		val debugVariant = testedVariant.get()
		val instrument = instrumentTestTask.get()
		val deviceProvider = instrument.deviceProviderFactory.getDeviceProvider(adb, null)
		val device = try {
			deviceProvider.init()
			deviceProvider.devices.single() as ConnectedDevice
		} finally {
			deviceProvider.terminate()
		}
		val realDevice = device.iDevice

		val testApk = debugVariant.androidTest!!.artifacts.apk
		val testApplicationId = debugVariant.androidTest!!.applicationId.get()
		logger.info("Uninstalling test package: ${testApplicationId}")
		realDevice.uninstallPackage(testApplicationId)
		logger.info("Installing test package: ${testApk}")
		realDevice.installPackage(testApk.absolutePath, false)

		val services = debugVariant.services

		val results = services.projectInfo.getTestResultsFolder()!!.resolve("upgrade-tests")
			.also { FileUtils.cleanOutputDir(it) }

		val testListener = TestAwareCustomTestRunListener(
			device.name, services.projectInfo.name, debugVariant.name, StdLogger(StdLogger.Level.VERBOSE)
		).apply {
			setReportDir(results)
		}

		val reports = services.projectInfo.getReportsDir().resolve("upgrade-tests")
			.also { FileUtils.cleanOutputDir(it) }

		var finished = false
		try {
			installOld(realDevice, debugVariant, "10001934-v1.0.0#1934")
			pushData(realDevice, debugVariant, "10001934-v1.0.0#1934")
			runTest(
				instrument.testData.get(), device, testListener, reports.resolve("index.html"),
				"net.twisterrob.inventory.android.UpgradeTests#testPrepareVersion1"
			)
			val newApk = debugVariant.artifacts.apk
			logger.info("Installing package: ${newApk}")
			realDevice.installPackage(newApk.absolutePath, true)
			runTest(
				instrument.testData.get(), device, testListener, reports.resolve("index.html"),
				"net.twisterrob.inventory.android.UpgradeTests#testVerifyVersion2"
			)
			finished = true
		} finally {
			try {
				val report = ResilientTestReport(ReportType.SINGLE_FLAVOR, results, reports)
				report.generateReport()
			} catch (@Suppress("TooGenericExceptionCaught") ex: Throwable) {
				if (finished) { // Swallow if there's already a failure.
					@Suppress("ThrowingExceptionFromFinally") // Safe, see condition.
					throw ex
				}
			}
		}
	}

	private fun installOld(realDevice: IDevice, debugVariant: ApplicationVariant, version: String) {
		val applicationId = debugVariant.applicationId.get()
		// FIXME release debug build as well
		val oldApk =
			File("${System.getenv("RELEASE_HOME")}/android/${applicationId}@${version}d+debug.apk")
		logger.info("Uninstalling package: ${applicationId}")
		realDevice.uninstallPackage(applicationId)
		logger.info("Installing package: ${oldApk}")
		realDevice.installPackage(oldApk.absolutePath, false)
	}

	private fun pushData(realDevice: IDevice, debugVariant: ApplicationVariant, version: String) {
		val applicationId = debugVariant.applicationId.get()
		val localData =
			File("${System.getenv("RELEASE_HOME")}/android/${applicationId}@${version}d+debug-data.zip")
		logger.info("Pushing ${localData}")
		@Suppress("SdCardPath") // False positive, this is Gradle code not Android.
		realDevice.pushFile(localData.absolutePath, "/sdcard/Download/data.zip")
	}

	private fun runTest(
		testData: TestData,
		device: DeviceConnector,
		runListener: TestAwareCustomTestRunListener,
		results: File,
		test: String
	) {
		runListener.setTest(test)
		// from com.android.builder.internal.testing.SimpleTestCallable#call
		val runner = RemoteAndroidTestRunner(
			testData.applicationId.get(),
			testData.instrumentationRunner.get(),
			device
		)
		testData.instrumentationRunnerArguments.forEach(runner::addInstrumentationArg)
		runner.addInstrumentationArg("class", test)
		//runner.addInstrumentationArg("annotation", "org.junit.Test")
		runner.addInstrumentationArg("upgrade", "true")
		runner.setMaxTimeToOutputResponse(1, TimeUnit.MINUTES)

		logger.info("Running: ${runner.amInstrumentCommand}")
		runner.run(runListener)

		val result = runListener.runResult
		@Suppress("ComplexCondition")
		if (result.hasFailedTests()
			|| result.isRunFailure
			|| result.numTests <= 0
			|| result.numCompleteTests != result.numTests
		) {
			throw GradleException("Tests failed, see ${results}")
		}
	}
}

val Variant.services: VariantServices
	get() = ComponentImpl::class.java
		.getDeclaredField("internalServices")
		.apply { isAccessible = true }
		.get(this) as VariantServices

val Artifacts.apk: File
	get() = this.get(SingleArtifact.APK).get().asFileTree.singleFile
