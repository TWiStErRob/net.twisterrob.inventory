package net.twisterrob.inventory.android.activity.space

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.update
import kotlin.reflect.KClass

abstract class BaseViewModel<Action : Any, State : Any, Effect : Any, Reduce : Reducer<State>>(
	private val effectHandler: EffectHandler<Effect>,
	initialState: State
) : ViewModel() {

	protected abstract val actionHandlers: Map<KClass<out Action>, ActionHandler<out Action, State, Effect, Reduce>>

	private val _state = MutableStateFlow(initialState)
	val state: StateFlow<State>
		get() = _state

	val effect: Flow<Effect>
		get() = effectHandler.effects

	suspend fun action(action: Action) {
		actionHandlers
			.getValue(action::class)
			.let { @Suppress("UNCHECKED_CAST") (it as ActionHandler<Action, State, Effect, Reduce>) }
			.handle(action, _state.value, effectHandler::handleEffect)
			.cancellable()
			.collect { reducer -> _state.update { reducer.reduce(_state.value) } }
	}
}

fun interface ActionHandler<Action, State, Effect, Reduce : Reducer<State>> {
	fun handle(action: Action, state: State, effect: suspend (Effect) -> Unit): Flow<Reduce>
}

abstract class EffectHandler<Effect> {
	internal val effects = MutableSharedFlow<Effect>()

	abstract suspend fun handleEffect(effect: Effect)
}

fun interface Reducer<State> {
	fun reduce(state: State): State
}

fun interface Mapper<Input, Output> {
	fun map(left: Input): Output
}

fun interface UseCase<Input, Output> {
	suspend fun execute(input: Input): Output
}
