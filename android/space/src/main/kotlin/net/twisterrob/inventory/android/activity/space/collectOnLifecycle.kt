package net.twisterrob.inventory.android.activity.space

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

context(LifecycleOwner)
fun <T> StateFlow<T>.collectOnLifecycle(
	state: Lifecycle.State = Lifecycle.State.STARTED,
	block: (T) -> Unit
) {
	this@LifecycleOwner.lifecycleScope.launch {
		this@LifecycleOwner.repeatOnLifecycle(state) {
			this@StateFlow.collect(block)
		}
	}
}
