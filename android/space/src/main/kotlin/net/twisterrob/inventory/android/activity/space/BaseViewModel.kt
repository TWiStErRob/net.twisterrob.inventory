package net.twisterrob.inventory.android.activity.space

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

abstract class BaseViewModel<State : Any, Effect : Any>(
	initialState: State
) : ViewModel(), ContainerHost<State, Effect> {

	override val container = container<State, Effect>(initialState)
}

fun interface EffectHandler<Effect> {
	suspend fun handleEffect(effect: Effect)
}

fun interface Mapper<Input, Output> {
	fun map(input: Input): Output
}

fun interface UseCase<Input, Output> {
	suspend fun execute(input: Input): Output
}
