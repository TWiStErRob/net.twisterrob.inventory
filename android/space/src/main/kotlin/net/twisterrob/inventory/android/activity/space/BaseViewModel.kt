package net.twisterrob.inventory.android.activity.space

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.update
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

abstract class BaseViewModel<Action : Any, State : Any, Effect : Any, Reduce : Reducer<State>>(
	private val effectHandler: EffectHandler<Effect>,
	initialState: State
) : ViewModel() {

	private val LOG = LoggerFactory.getLogger(this::class.java)

	protected abstract val actionHandlers: Map<KClass<out Action>, ActionHandler<out Action, State, Effect, Reduce>>

	private val _state = MutableStateFlow(initialState)
	val state: StateFlow<State>
		get() = _state

	val effect: Flow<Effect>
		get() = effectHandler.effects

	suspend fun action(action: Action) {
		LOG.trace("Action: {}", action)
		actionHandlers
			.getValue(action::class)
			.let { @Suppress("UNCHECKED_CAST") (it as ActionHandler<Action, State, Effect, Reduce>) }
			.also { LOG.trace("Found handler: {}", it) }
			.handle(action, _state.value, effectHandler::handleEffect)
			.cancellable()
			.collect { reducer ->
				LOG.trace("Reducer: {}", reducer)
				_state.update {oldState ->
					val newState = reducer.reduce(_state.value)
					LOG.trace("Updating state from\n{}\nto\n{}", oldState, newState)
					return@update newState
				}
			}
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
	fun map(input: Input): Output
}

fun interface UseCase<Input, Output> {
	suspend fun execute(input: Input): Output
}
