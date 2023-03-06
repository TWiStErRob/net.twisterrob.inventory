import com.android.build.gradle.internal.testing.*
import com.android.utils.*

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
