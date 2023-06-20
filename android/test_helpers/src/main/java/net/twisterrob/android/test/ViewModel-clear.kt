package net.twisterrob.android.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

fun ViewModel.clear() {
	val viewModelStore = ViewModelStore()
	val viewModelProvider = ViewModelProvider(viewModelStore, object : ViewModelProvider.Factory {
		@Suppress("UNCHECKED_CAST")
		override fun <T : ViewModel> create(modelClass: Class<T>): T = this@clear as T
	})
	// Simulate creation.
	viewModelProvider[this@clear::class.java]

	// Call ViewModel.clear() indirectly.
	viewModelStore.clear()
}
