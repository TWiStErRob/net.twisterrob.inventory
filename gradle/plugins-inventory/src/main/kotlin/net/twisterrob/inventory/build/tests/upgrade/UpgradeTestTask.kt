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
import java.io.File

@Suppress("UnstableApiUsage")
abstract class UpgradeTestTask : DefaultTask() {
	init {
		dependsOn("assembleDebug") // TODO why this is not available at eval time?
		dependsOn(project.tasks.named("assembleAndroidTest"))
	}

	@TaskAction
	fun upgradeTest() {
		val android = project.extensions.findByName("android") as AppExtension
		val debugVariant = android
				.applicationVariants
				.filter { it.buildType.name == "debug" }
				.single() as ApplicationVariantImpl

		val instrument = debugVariant.testVariant.connectedInstrumentTestProvider.get() as
				DeviceProviderInstrumentTestTask

		instrument.deviceProvider.init()
		val device = instrument.deviceProvider.devices.first() as ConnectedDevice
		val realDevice = device.getIDevice()
		instrument.deviceProvider.terminate()

		val testApk = debugVariant.testVariant.outputs.first().outputFile
		logger.info("Uninstalling test package: ${debugVariant.testVariant.applicationId}")
		realDevice.uninstallPackage(debugVariant.testVariant.applicationId)
		logger.info("Installing test package: ${testApk}")
		realDevice.installPackage(testApk.absolutePath, false)

		val data: BaseVariantData = debugVariant.variantData
		@Suppress("DEPRECATION")
		val results = data.globalScope.testResultsFolder.resolve("upgrade-tests")
		@Suppress("DEPRECATION")
		val reports = data.globalScope.reportsDir.resolve("upgrade-tests")
		FileUtils.cleanOutputDir(results)
		FileUtils.cleanOutputDir(reports)
		val testListener = TestAwareCustomTestRunListener(
				device.name, project.name, debugVariant.name, StdLogger(StdLogger.Level.VERBOSE))
		testListener.setReportDir(results)

		var failed = false
		try {
			installOld(realDevice, debugVariant, "10001934-v1.0.0#1934")
			pushData(realDevice, debugVariant, "10001934-v1.0.0#1934")
			runTest(instrument.testData, device, testListener, reports.resolve("index.html"),
					"net.twisterrob.inventory.android.UpgradeTests#testPrepareVersion1")

			val newApk = debugVariant.outputs.first().outputFile
			logger.info("Installing package: ${newApk}")
			realDevice.installPackage(newApk.absolutePath, true)
			runTest(instrument.testData, device, testListener, reports.resolve("index.html"),
					"net.twisterrob.inventory.android.UpgradeTests#testVerifyVersion2")
		} catch (ex: Throwable) {
			failed = true
			throw ex
		} finally {
			try {
				val report = ResilientTestReport(ReportType.SINGLE_FLAVOR, results, reports)
				report.generateReport()
			} catch (ex: Throwable) {
				if (!failed) { // swallow if there's already a failure
					throw ex
				}
			}
		}
	}

	private fun installOld(realDevice: IDevice, debugVariant: ApplicationVariant, version: String) {
		// FIXME release debug build as well
		val oldApk = project.file("${System.getenv("RELEASE_HOME")}/android/${debugVariant.applicationId}@${version}d+debug.apk")
		logger.info("Unnstalling package: ${debugVariant.applicationId}")
		realDevice.uninstallPackage(debugVariant.applicationId)
		logger.info("Installing package: ${oldApk}")
		realDevice.installPackage(oldApk.absolutePath, false)
	}
	private fun pushData(realDevice: IDevice, debugVariant: ApplicationVariant, version: String) {
		val localData = "${System.getenv("RELEASE_HOME")}/android/${debugVariant.applicationId}@${version}d+debug-data.zip"
		logger.info("Pushing ${localData}")
		realDevice.pushFile(localData, "/sdcard/Download/data.zip")
	}
	private fun runTest(testData: TestData, device: DeviceConnector,
			runListener: TestAwareCustomTestRunListener, results: File, test: String) {
		runListener.setTest(test)
		// from com.android.builder.internal.testing.SimpleTestCallable#call
		val runner = RemoteAndroidTestRunner(testData.applicationId.get(), testData.instrumentationRunner.get(), device)
		testData.instrumentationRunnerArguments.forEach { (k, v) -> runner.addInstrumentationArg(k, v) }
		runner.addInstrumentationArg("class", test)
		//runner.addInstrumentationArg("annotation", "org.junit.Test")
		runner.addInstrumentationArg("upgrade", "true")
		@Suppress("DEPRECATION")
		runner.setMaxtimeToOutputResponse(60000)

		logger.info("Running: ${runner.amInstrumentCommand}")
		runner.run(runListener)

		val result = runListener.runResult
		if (result.hasFailedTests()
				|| result.isRunFailure()
				|| result.getNumTests() <= 0
				|| result.numCompleteTests != result.numTests) {
			throw GradleException("Tests failed, see ${results}")
		}
	}
}
