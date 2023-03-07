package net.twisterrob.inventory.build.tests.upgrade

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.build.gradle.internal.test.report.ReportType
import com.android.build.gradle.internal.test.report.ResilientTestReport
import com.android.build.gradle.internal.testing.ConnectedDevice
import com.android.build.gradle.internal.testing.TestData
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.testing.api.DeviceConnector
import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.android.utils.FileUtils
import com.android.utils.StdLogger
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.util.concurrent.TimeUnit

@Suppress("UnstableApiUsage")
@DisableCachingByDefault(because = "Lots of external factors")
abstract class UpgradeTestTask : DefaultTask() {

	@Suppress("LongMethod") // Will be split up when I make it work again.
	@TaskAction
	fun upgradeTest() {
		val android = project.extensions.findByName("android") as AppExtension
		val debugVariant = android
			.applicationVariants
			.single { it.buildType.name == "debug" }
			as ApplicationVariantImpl

		val instrument = debugVariant.testVariant.connectedInstrumentTestProvider.get() as
			DeviceProviderInstrumentTestTask
		val device = try {
			instrument.deviceProvider.init()
			instrument.deviceProvider.devices.single() as ConnectedDevice
		} finally {
			instrument.deviceProvider.terminate()
		}
		val realDevice = device.iDevice

		val testApk = debugVariant.testVariant.outputs.first().outputFile
		logger.info("Uninstalling test package: ${debugVariant.testVariant.applicationId}")
		realDevice.uninstallPackage(debugVariant.testVariant.applicationId)
		logger.info("Installing test package: ${testApk}")
		realDevice.installPackage(testApk.absolutePath, false)

		val data: BaseVariantData = debugVariant.variantData

		@Suppress("DEPRECATION")
		val results = data.globalScope.testResultsFolder.resolve("upgrade-tests")
			.also { FileUtils.cleanOutputDir(it) }

		val testListener = TestAwareCustomTestRunListener(
			device.name, project.name, debugVariant.name, StdLogger(StdLogger.Level.VERBOSE)
		).apply {
			setReportDir(results)
		}

		@Suppress("DEPRECATION")
		val reports = data.globalScope.reportsDir.resolve("upgrade-tests")
			.also { FileUtils.cleanOutputDir(it) }

		var finished = false
		try {
			installOld(realDevice, debugVariant, "10001934-v1.0.0#1934")
			pushData(realDevice, debugVariant, "10001934-v1.0.0#1934")
			runTest(
				instrument.testData, device, testListener, reports.resolve("index.html"),
				"net.twisterrob.inventory.android.UpgradeTests#testPrepareVersion1"
			)
			val newApk = debugVariant.outputs.first().outputFile
			logger.info("Installing package: ${newApk}")
			realDevice.installPackage(newApk.absolutePath, true)
			runTest(
				instrument.testData, device, testListener, reports.resolve("index.html"),
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
		// FIXME release debug build as well
		val oldApk =
			File("${System.getenv("RELEASE_HOME")}/android/${debugVariant.applicationId}@${version}d+debug.apk")
		logger.info("Uninstalling package: ${debugVariant.applicationId}")
		realDevice.uninstallPackage(debugVariant.applicationId)
		logger.info("Installing package: ${oldApk}")
		realDevice.installPackage(oldApk.absolutePath, false)
	}

	private fun pushData(realDevice: IDevice, debugVariant: ApplicationVariant, version: String) {
		val localData =
			File("${System.getenv("RELEASE_HOME")}/android/${debugVariant.applicationId}@${version}d+debug-data.zip")
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
