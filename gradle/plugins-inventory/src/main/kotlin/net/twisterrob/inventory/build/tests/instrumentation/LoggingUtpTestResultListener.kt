package net.twisterrob.inventory.build.tests.instrumentation

import com.android.build.gradle.internal.testing.utp.UtpTestResultListener
import com.android.tools.utp.plugins.result.listener.gradle.proto.GradleAndroidTestResultListenerProto.TestResultEvent
import com.android.tools.utp.plugins.result.listener.gradle.proto.GradleAndroidTestResultListenerProto.TestResultEvent.StateCase
import com.google.testing.platform.proto.api.core.TestCaseProto.TestCase
import com.google.testing.platform.proto.api.core.TestResultProto.TestResult
import com.google.testing.platform.proto.api.core.TestStatusProto.TestStatus
import org.gradle.api.logging.Logger

/**
 * See https://issuetracker.google.com/issues/37056080#comment12.
 */
class LoggingUtpTestResultListener(
	private val logger: Logger
) : UtpTestResultListener {
	// See com.android.build.gradle.internal.testing.utp.DdmlibTestResultAdapter.onTestResultEvent
	// See com.android.build.gradle.internal.testing.CustomTestRunListener.testEnded
	override fun onTestResultEvent(testResultEvent: TestResultEvent) {
		//logger.lifecycle("UTP: {}", testResultEvent)
		when (testResultEvent.stateCase) {
			StateCase.TEST_CASE_STARTED -> {
				val tc = testResultEvent.testCaseStarted.testCase.unpack(TestCase::class.java)
				logger.lifecycle("${tc.testPackage}.${tc.testClass} > ${tc.testMethod} \u001b[34mSTARTED\u001b[0m")
			}
			StateCase.TEST_CASE_FINISHED -> {
				val result = testResultEvent.testCaseFinished.testCaseResult.unpack(TestResult::class.java)
				val status = when (result.testStatus) {
					TestStatus.PASSED -> "\u001b[32mSUCCESS\u001b[0m"
					// Already handled by AGP.
					TestStatus.FAILED, TestStatus.ERROR, TestStatus.IGNORED -> null
					else -> "\u001b[31${result.testStatus.name}\u001b[0m"
				}
				if (status != null) {
					val tc = result.testCase
					logger.lifecycle("${tc.testPackage}.${tc.testClass} > ${tc.testMethod} ${status}")
				}
			}
			else -> {}
		}
	}
}
