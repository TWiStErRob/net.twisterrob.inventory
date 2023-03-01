import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.build.gradle.internal.test.report.*
import com.android.build.gradle.internal.testing.*
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.builder.testing.api.DeviceConnector
import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.android.utils.*
import org.gradle.api.*
import org.gradle.api.tasks.TaskAction

class UpgradeTestTask extends DefaultTask {
	UpgradeTestTask() {
		dependsOn 'assembleDebug' // TODO why this is not available at eval time?
		dependsOn project.tasks.named("assembleAndroidTest")
	}

	@TaskAction
	@SuppressWarnings('GrDeprecatedAPIUsage')
	def upgradeTest() {
		def android = project.extensions.findByName("android") as AppExtension
		def debugVariant = android
				.applicationVariants
				.grep { ApplicationVariant var -> var.buildType.name == 'debug' }
				.first() as ApplicationVariantImpl

		def instrument = debugVariant.testVariant.connectedInstrumentTestProvider.get() as
				DeviceProviderInstrumentTestTask

		instrument.deviceProvider.init()
		def device = instrument.deviceProvider.devices.first() as ConnectedDevice
		IDevice realDevice = device.getIDevice()
		instrument.deviceProvider.terminate()

		File testApk = debugVariant.testVariant.outputs.first().outputFile
		logger.info("Uninstalling test package: ${debugVariant.testVariant.applicationId}")
		realDevice.uninstallPackage(debugVariant.testVariant.applicationId)
		logger.info("Installing test package: ${testApk}")
		realDevice.installPackage(testApk.absolutePath, false, null)

		BaseVariantData data = debugVariant.variantData
		def results = new File(data.services.projectInfo.testResultsFolder, 'upgrade-tests')
		def reports = new File(data.services.projectInfo.reportsDir, 'upgrade-tests')
		FileUtils.cleanOutputDir(results)
		FileUtils.cleanOutputDir(reports)
		def testListener = new TestAwareCustomTestRunListener(
				device.name, project.name, debugVariant.name, new StdLogger(StdLogger.Level.VERBOSE))
		testListener.reportDir = results

		boolean failed = false
		try {
			installOld(realDevice, debugVariant, '10001934-v1.0.0#1934')
			pushData(realDevice, debugVariant, '10001934-v1.0.0#1934')
			runTest(instrument.testData, device, testListener, new File(reports, 'index.html'),
					"net.twisterrob.inventory.android.UpgradeTests#testPrepareVersion1")

			File newApk = debugVariant.outputs.first().outputFile
			logger.info("Installing package: ${newApk}")
			realDevice.installPackage(newApk.absolutePath, true, null)
			runTest(instrument.testData, device, testListener, new File(reports, 'index.html'),
					"net.twisterrob.inventory.android.UpgradeTests#testVerifyVersion2")
		} catch (Throwable ex) {
			failed = true
			throw ex
		} finally {
			try {
				ResilientTestReport report = new ResilientTestReport(ReportType.SINGLE_FLAVOR, results, reports)
				report.generateReport()
			} catch (Throwable ex) {
				if (!failed) { // swallow if there's already a failure
					throw ex
				}
			}
		}
	}

	private def installOld(IDevice realDevice, ApplicationVariant debugVariant, String version) {
		// FIXME release debug build as well
		File oldApk = project.file("${System.getenv("RELEASE_HOME")}/android/${debugVariant.applicationId}@${version}d+debug.apk")
		logger.info("Unnstalling package: ${debugVariant.applicationId}")
		realDevice.uninstallPackage(debugVariant.applicationId)
		logger.info("Installing package: ${oldApk}")
		realDevice.installPackage(oldApk.absolutePath, false, null)
	}
	private def pushData(IDevice realDevice, ApplicationVariant debugVariant, String version) {
		def localData = "${System.getenv("RELEASE_HOME")}/android/${debugVariant.applicationId}@${version}d+debug-data.zip"
		logger.info("Pushing ${localData}")
		realDevice.pushFile(localData, "/sdcard/Download/data.zip")
	}
	private def runTest(TestData testData, DeviceConnector device,
			TestAwareCustomTestRunListener runListener, File results, String test) {
		runListener.test = test
		// from com.android.builder.internal.testing.SimpleTestCallable#call
		def runner = new RemoteAndroidTestRunner(testData.applicationId.get(), testData.instrumentationRunner.get(), device)
		for (Map.Entry<String, String> argument : testData.instrumentationRunnerArguments.entrySet()) {
			runner.addInstrumentationArg(argument.getKey(), argument.getValue())
		}
		runner.addInstrumentationArg('class', test)
		//runner.addInstrumentationArg('annotation', 'org.junit.Test')
		runner.addInstrumentationArg('upgrade', 'true')
		runner.maxtimeToOutputResponse = 60000

		logger.info("Running: ${runner.amInstrumentCommand}")
		runner.run runListener

		def result = runListener.runResult
		if (result.hasFailedTests()
				|| result.isRunFailure()
				|| result.getNumTests() <= 0
				|| result.numCompleteTests != result.numTests) {
			throw new GradleException("Tests failed, see ${results}")
		}
	}
}

class TestAwareCustomTestRunListener extends CustomTestRunListener {
	TestAwareCustomTestRunListener(String deviceName, String projectName, String flavorName, ILogger logger) {
		super(deviceName, projectName, flavorName, logger)
	}

	@Override protected File getResultFile(File reportDir) throws IOException {
		File resultFile = super.getResultFile(reportDir)
		return new File(resultFile.parentFile, addTestName(resultFile.name))
	}

	private String test

	void setTest(String test) {
		this.test = test
	}

	private String addTestName(String name) {
		def suffix = '.xml'
		if (this.test != null && name.endsWith(suffix)) {
			def onlyName = name.substring(0, name.length() - suffix.length())
			name = "${onlyName}-${test}${suffix}"
		}
		return name
	}
}
