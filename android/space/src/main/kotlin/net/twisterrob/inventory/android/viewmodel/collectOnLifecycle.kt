package net.twisterrob.inventory.android.viewmodel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// STOPSHIP move to lib
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
