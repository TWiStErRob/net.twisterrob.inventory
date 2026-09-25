package net.twisterrob.inventory.build.tests

import com.android.build.gradle.tasks.TestSuiteTestTask
import org.gradle.api.tasks.testing.logging.TestLogEvent

// TODEL https://issuetracker.google.com/issues/37056080#comment14
tasks.withType<TestSuiteTestTask>().configureEach {
	testLogging {
		showStandardStreams = true
		events = TestLogEvent.entries.toSet()
	}
}
