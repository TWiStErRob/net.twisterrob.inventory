package net.twisterrob.inventory.android.space

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MainDispatcherRule(
	val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
	override fun starting(description: Description) {
		@OptIn(ExperimentalCoroutinesApi::class)
		Dispatchers.setMain(testDispatcher)
	}

	override fun finished(description: Description) {
		@OptIn(ExperimentalCoroutinesApi::class)
		Dispatchers.resetMain()
	}
}
