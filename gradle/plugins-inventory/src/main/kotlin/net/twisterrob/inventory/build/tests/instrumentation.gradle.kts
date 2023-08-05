package net.twisterrob.inventory.build.tests

import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import net.twisterrob.inventory.build.tests.instrumentation.LoggingUtpTestResultListener

// TODEL https://issuetracker.google.com/issues/37056080
tasks.withType<DeviceProviderInstrumentTestTask>().configureEach {
	setUtpTestResultListener(LoggingUtpTestResultListener(logger))
}
