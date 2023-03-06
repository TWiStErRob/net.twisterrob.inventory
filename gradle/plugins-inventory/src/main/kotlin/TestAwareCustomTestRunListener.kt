import com.android.build.gradle.internal.testing.*
import com.android.utils.*
import java.io.File
import java.io.IOException

class TestAwareCustomTestRunListener(
	deviceName: String,
	projectName: String,
	flavorName: String,
	logger: ILogger,
) : CustomTestRunListener(deviceName, projectName, flavorName, logger) {

	@kotlin.jvm.Throws(IOException::class)
	override fun getResultFile(reportDir: File): File {
		val resultFile = super.getResultFile(reportDir)
		return resultFile.parentFile.resolve(addTestName(resultFile.name))
	}

	private var test: String? = null

	fun setTest(test: String) {
		this.test = test
	}

	private fun addTestName(name: String): String {
		@Suppress("NAME_SHADOWING")
		var name = name
		val suffix = ".xml"
		if (this.test != null && name.endsWith(suffix)) {
			val onlyName = name.substring(0, name.length - suffix.length)
			name = "${onlyName}-${test}${suffix}"
		}
		return name
	}
}
